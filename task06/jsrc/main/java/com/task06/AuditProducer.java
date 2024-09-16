package com.task06;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemUtils;
import com.amazonaws.services.dynamodbv2.document.Table;
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
import org.apache.commons.codec.binary.StringUtils;

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

@DynamoDbTriggerEventSource(targetTable = "Configuration", batchSize = 1)
@DependsOn(name = "Configuration", resourceType = ResourceType.DYNAMODB_TABLE)

@EnvironmentVariables(value = {
		@EnvironmentVariable(key = "region", value = "${region}"),
		@EnvironmentVariable(key = "target_table", value = "${target_table}")
})

public class AuditProducer implements RequestHandler<DynamodbEvent, Void> {

	private static final String INSERT = "INSERT";
	public Void handleRequest(DynamodbEvent dynamodbEvent, Context context) {
		System.out.println("Hello from lambda : AuditProducer");
		String tableName = System.getenv("target_table");
		String region = System.getenv("region");
		String createdOrUpdatedAt = Instant.now().toString();
		DynamoDB dynamoDB = new DynamoDB(AmazonDynamoDBAsyncClientBuilder.standard().withRegion(region).build());
		Table table = dynamoDB.getTable(tableName);

		for (DynamodbEvent.DynamodbStreamRecord record : dynamodbEvent.getRecords()) {
			if (INSERT.equals(record.getEventName())) {
				System.out.println("record going to be inserted in audit table.");
				Map<String, AttributeValue> recordMap = record.getDynamodb().getNewImage();

                String itemKeyValue = "";
				for (Map.Entry<String, AttributeValue> entry : recordMap.entrySet()) {
					if("key".equalsIgnoreCase(entry.getKey())){
						itemKeyValue = entry.getValue().getS();
					}
				}

				String id = UUID.randomUUID().toString();
				Item item = new Item().withPrimaryKey("id", id)
						.withString("itemKey", itemKeyValue)
						.withString("modificationTime", createdOrUpdatedAt);
						//.with("newValue", recordMap);
				table.putItem(item);
			}
		}
		return null;
	}
}
