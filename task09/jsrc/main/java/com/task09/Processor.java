package com.task09;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.TracingMode;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import com.weather.WeatherData;
import org.json.simple.JSONObject;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@LambdaHandler(
    lambdaName = "processor",
	roleName = "processor-role",
	layers = {"sdk-layer"},
	runtime = DeploymentRuntime.JAVA17,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED,
	tracingMode = TracingMode.Active
)

@LambdaLayer(
		layerName = "sdk-layer",
		libraries = {"lib/WeatherAPI.jar"},
		runtime = DeploymentRuntime.JAVA17
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)

@EnvironmentVariables(value = {
		@EnvironmentVariable(key = "region", value = "${region}"),
		@EnvironmentVariable(key = "target_table", value = "${target_table}")
})

public class Processor implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	private static final int SC_OK = 200;
	private static final int SC_NOT_FOUND = 400;
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private final Map<String, String> responseHeaders = Map.of("Content-Type", "application/json");
	private final Map<RouteKey, Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse>> routeHandlers = Map.of(
			new RouteKey("GET", "/"), this::saveWeatherDataToDynamoDbTable);

	public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent requestEvent, Context context) {
		System.out.println("lambda called via functional url");
		RouteKey routeKey = new RouteKey(getMethod(requestEvent), getPath(requestEvent));
		return routeHandlers.getOrDefault(routeKey, this::notFoundResponse).apply(requestEvent);
	}

	private APIGatewayV2HTTPResponse saveWeatherDataToDynamoDbTable(APIGatewayV2HTTPEvent requestEvent) {
		System.out.println("going to store weather forecast to dynamodb table");
		String tableName = System.getenv("target_table");
		String region = System.getenv("region");
		String id = UUID.randomUUID().toString();
		String createdAt = Instant.now().toString();
		DynamoDB dynamoDB = new DynamoDB(AmazonDynamoDBAsyncClientBuilder.standard().withRegion(region).build());
		Table table = dynamoDB.getTable(tableName);

		try {
			WeatherData weatherData = new WeatherData();
			JSONObject weatherResponse = weatherData.getWeatherData();
			var responseMap = new HashMap<String, Object>();

			//hourly_units
			JSONObject hourlyUnits = (JSONObject) weatherResponse.get("hourly_units");
			var hourlyUnitsMap = new HashMap<String, Object>();
			hourlyUnitsMap.put("time", hourlyUnits.get("time"));
			hourlyUnitsMap.put("temperature_2m", hourlyUnits.get("temperature_2m"));

			//hourly
			JSONObject hourly = (JSONObject) weatherResponse.get("hourly");
			var hourlyMap = new HashMap<String, Object>();
			hourlyMap.put("time", hourly.get("time"));
			hourlyMap.put("temperature_2m", hourly.get("temperature_2m"));


			responseMap.put("elevation", weatherResponse.get("elevation"));
			responseMap.put("generationtime_ms", weatherResponse.get("generationtime_ms"));
			responseMap.put("timezone_abbreviation", weatherResponse.get("timezone_abbreviation"));
			responseMap.put("timezone", weatherResponse.get("timezone"));
			responseMap.put("latitude", weatherResponse.get("latitude"));
			responseMap.put("longitude", weatherResponse.get("longitude"));
			responseMap.put("hourly_units", hourlyUnitsMap);
			responseMap.put("hourly", hourlyMap);
			var objectMapper = new ObjectMapper();
			var finalResponse = objectMapper.writeValueAsString(responseMap);
			System.out.println(finalResponse);

			//saving data to dynamodb table
			Item item = new Item().withPrimaryKey("id", id)
					.with("forecast", finalResponse);
			table.putItem(item);

		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}

		return buildResponse(SC_OK, Body.ok(SC_OK, "Data saved to Dynamodb table - Weather"));
	}

	private record RouteKey(String method, String path) {
	}

	private record Body(int statusCode, String message, String error) {
		static Body ok(int statusCode, String message) {
			return new Body(statusCode, message, null);
		}
	}

	private String getMethod(APIGatewayV2HTTPEvent requestEvent) {
		return requestEvent.getRequestContext().getHttp().getMethod();
	}

	private String getPath(APIGatewayV2HTTPEvent requestEvent) {
		return requestEvent.getRequestContext().getHttp().getPath();
	}

	private APIGatewayV2HTTPResponse buildResponse(int statusCode, Object body) {
		return APIGatewayV2HTTPResponse.builder()
				.withStatusCode(statusCode)
				.withHeaders(responseHeaders)
				.withBody(gson.toJson(body))
				.build();
	}

	private APIGatewayV2HTTPResponse notFoundResponse(APIGatewayV2HTTPEvent requestEvent) {
		return buildResponse(SC_NOT_FOUND, Body.ok(SC_NOT_FOUND, "Bad request syntax or unsupported method. Request path: %s. HTTP method: %s".formatted(
				getPath(requestEvent),
				getMethod(requestEvent)
		)));
	}
}
