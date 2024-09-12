package com.task05;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.LambdaSnapStart;
import com.syndicate.deployment.model.ResourceType;
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

@DependsOn(name = "Events", resourceType = ResourceType.DYNAMODB_TABLE)

@EnvironmentVariables(value = {
		@EnvironmentVariable(key = "region", value = "${region}"),
		@EnvironmentVariable(key = "target_table", value = "${table_name}")
})


public class ApiHandler implements RequestHandler<ApiGatewayRequest, Map<String, Object>> {

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
		resultMap.put("statusCode", 201);
		resultMap.put("event", item.asMap());
		return resultMap;
	}
}
