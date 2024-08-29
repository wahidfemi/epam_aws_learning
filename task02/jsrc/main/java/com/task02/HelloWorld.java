package com.task02;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.Architecture;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@LambdaHandler(
    lambdaName = "hello_world",
	roleName = "hello_world-role",
	runtime = DeploymentRuntime.JAVA17,
	architecture = Architecture.ARM64,
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)

@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
public class HelloWorld implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

	private static final int SC_OK = 200;
	private static final int SC_NOT_FOUND = 400;
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private final Map<String, String> responseHeaders = Map.of("Content-Type", "application/json");
	private final Map<RouteKey, Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse>> routeHandlers = Map.of(
			new RouteKey("GET", "/hello"), this::handleGetHello);

	@Override
	public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent requestEvent, Context context) {
		RouteKey routeKey = new RouteKey(getMethod(requestEvent), getPath(requestEvent));
		return routeHandlers.getOrDefault(routeKey, this::notFoundResponse).apply(requestEvent);
	}

	private APIGatewayV2HTTPResponse handleGetHello(APIGatewayV2HTTPEvent requestEvent) {
		return buildResponse(SC_OK, Body.ok(SC_OK, "Hello from Lambda"));
	}

	private APIGatewayV2HTTPResponse notFoundResponse(APIGatewayV2HTTPEvent requestEvent) {
		return buildResponse(SC_NOT_FOUND, Body.ok(SC_NOT_FOUND, "Bad request syntax or unsupported method. Request path: %s. HTTP method: %s".formatted(
				getPath(requestEvent),
				getMethod(requestEvent)
		)));
	}

	private APIGatewayV2HTTPResponse buildResponse(int statusCode, Object body) {
		return APIGatewayV2HTTPResponse.builder()
				.withStatusCode(statusCode)
				.withHeaders(responseHeaders)
				.withBody(gson.toJson(body))
				.build();
	}

	private String getMethod(APIGatewayV2HTTPEvent requestEvent) {
		return requestEvent.getRequestContext().getHttp().getMethod();
	}

	private String getPath(APIGatewayV2HTTPEvent requestEvent) {
		return requestEvent.getRequestContext().getHttp().getPath();
	}

	private String getUserName(Map<String, String> queryStringParameters) {
		return queryStringParameters.get("name");
	}


	private record RouteKey(String method, String path) {
	}

	private record Body(int statusCode, String message, String error) {
		static Body ok(int statusCode, String message) {
			return new Body(statusCode, message, null);
		}
	}

}
