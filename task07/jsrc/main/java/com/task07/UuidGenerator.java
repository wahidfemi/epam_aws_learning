package com.task07;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.events.RuleEventSource;
import com.syndicate.deployment.annotations.events.RuleEvents;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(
    lambdaName = "uuid_generator",
	roleName = "uuid_generator-role",
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)

@RuleEventSource(targetRule = "uuid_trigger")

@DependsOn(name = "uuid_trigger", resourceType = ResourceType.CLOUDWATCH_RULE)

@EnvironmentVariables(value = {
		@EnvironmentVariable(key = "region", value = "${region}"),
		@EnvironmentVariable(key = "target_bucket", value = "${target_bucket}")
})

public class UuidGenerator implements RequestHandler<ScheduledEvent, Void> {

	private final AmazonS3 s3Client;
	private final ObjectMapper objectMapper;

	public UuidGenerator() {
		this.s3Client = AmazonS3Client.builder().withRegion(System.getenv("region")).build();
		this.objectMapper = new ObjectMapper();
	}

	public Void handleRequest(ScheduledEvent scheduledEvent, Context context) {
		System.out.println("Hello from lambda");
		String bucketName = System.getenv("target_bucket");
		String fileName = Instant.now().toString();

		Map<String, Object> fileContent = new HashMap<String, Object>();
		String[] uuids = new String[10];
		for(int i=0;i<10;i++){
			uuids[i] = UUID.randomUUID().toString();
		}

		fileContent.put("ids", uuids);

		s3Client.putObject(bucketName, fileName,
				convertObjectToJson(fileContent));

		return null;
	}

	private String convertObjectToJson(Object object) {
		try {
			return objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("Object cannot be converted to JSON: " + object);
		}
	}
}
