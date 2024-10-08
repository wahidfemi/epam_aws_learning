package com.task05;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(
    lambdaName = "api_handler",
	roleName = "api_handler-role",
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)

@EnvironmentVariables(value = {
		@EnvironmentVariable(key = "region", value = "${region}"),
		@EnvironmentVariable(key = "target_table", value = "${target_table}")
})


public class ApiHandler implements RequestHandler<ApiGatewayRequest, Map<String, Object>> {

	private static final int SC_CREATED = 201;

	public Map<String, Object> handleRequest(ApiGatewayRequest apiRequest, Context context) {
		String tableName = System.getenv("target_table");
		String region = System.getenv("region");
		String id = UUID.randomUUID().toString();
		String createdAt = Instant.now().toString();
		DynamoDB dynamoDB = new DynamoDB(AmazonDynamoDBAsyncClientBuilder.standard().withRegion(region).build());
		Table table = dynamoDB.getTable(tableName);
		Item item = new Item().withPrimaryKey("id", id)
				.withInt("principalId", apiRequest.getPrincipalId())
				.withString("createdAt", createdAt)
				.with("body", apiRequest.getContent());
		table.putItem(item);


		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("statusCode", SC_CREATED);
		resultMap.put("event", item.asMap());
		return resultMap;
	}
}
