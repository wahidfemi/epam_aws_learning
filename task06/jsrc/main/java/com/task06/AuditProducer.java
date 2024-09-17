package com.task06;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(
    lambdaName = "audit_producer",
	roleName = "audit_producer-role",
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)

@DynamoDbTriggerEventSource(targetTable = "Configuration", batchSize = 10)
@DependsOn(name = "Configuration", resourceType = ResourceType.DYNAMODB_TABLE)

@EnvironmentVariables(value = {
		@EnvironmentVariable(key = "region", value = "${region}"),
		@EnvironmentVariable(key = "target_table", value = "${target_table}")
})

public class AuditProducer implements RequestHandler<DynamodbEvent, Void> {

	private static final String INSERT = "INSERT";
	private static final String UPDATE = "MODIFY";
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public Void handleRequest(DynamodbEvent dynamodbEvent, Context context) {
		System.out.println("Hello from lambda : AuditProducer");
		String tableName = System.getenv("target_table");
		String region = System.getenv("region");
		String createdOrUpdatedAt = Instant.now().toString();
		DynamoDB dynamoDB = new DynamoDB(AmazonDynamoDBAsyncClientBuilder.standard().withRegion(region).build());
		Table table = dynamoDB.getTable(tableName);


		for (DynamodbEvent.DynamodbStreamRecord record : dynamodbEvent.getRecords()) {

			System.out.println(record.getEventID());
			System.out.println(record.getEventName());
			System.out.println("DynamoDB Record: " + GSON.toJson(record.getDynamodb()));

			if (INSERT.equals(record.getEventName())) {
				System.out.println("record going to be inserted in audit table.");
				Map<String, AttributeValue> recordMap = record.getDynamodb().getNewImage();
				String itemKeyValue = recordMap.get("key").getS();

				Map<String, Object> simpleMap = new HashMap<String, Object>();
                simpleMap.put("key", itemKeyValue);
				simpleMap.put("value", Integer.valueOf(recordMap.get("value").getN()));


				String id = UUID.randomUUID().toString();
				Item item = new Item().withPrimaryKey("id", id)
						.withString("itemKey", itemKeyValue)
						.withString("modificationTime", createdOrUpdatedAt)
						.with("newValue", simpleMap);
				table.putItem(item);
			}
			else if(UPDATE.equals(record.getEventName())){
				System.out.println("we are going to insert new record for Audit table");
				Map<String, AttributeValue> oldRecordMap = record.getDynamodb().getOldImage();
				Map<String, AttributeValue> newRecordMap = record.getDynamodb().getNewImage();
				String itemKeyValue = oldRecordMap.get("key").getS();
				String oldValue = oldRecordMap.get("value").getN();
				String newValue = newRecordMap.get("value").getN();
				String id = UUID.randomUUID().toString();
				Item item = new Item().withPrimaryKey("id", id)
						.withString("itemKey", itemKeyValue)
						.withString("modificationTime", createdOrUpdatedAt)
						.withString("updatedAttribute", "value")
						.withNumber("oldValue", Integer.parseInt(oldValue))
						.withNumber("newValue", Integer.parseInt(newValue));
				table.putItem(item);
			}
		}
		return null;
	}
}
