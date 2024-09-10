package com.task04;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.syndicate.deployment.annotations.events.SnsEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;

@LambdaHandler(
		lambdaName = "sns_handler",
		roleName = "sns_handler-role",
		isPublishVersion = false,
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)

@SnsEventSource(
		targetTopic = "lambda_topic"
)
@DependsOn(
		name = "lambda_topic",
		resourceType = ResourceType.SNS_TOPIC
)

public class SnsHandler implements RequestHandler<SNSEvent, Void> {

	@Override
	public Void handleRequest(SNSEvent snsEvent, Context context) {
		for (SNSEvent.SNSRecord record : snsEvent.getRecords()) {
			processMessage(record, context);
		}
		context.getLogger().log("done");
		return null;
	}

	private void processMessage(SNSEvent.SNSRecord record, Context context) {
		try {
			context.getLogger().log("SNS message " + record.getSNS().getMessage());
		} catch (Exception e) {
			context.getLogger().log("An error occurred for SNS message");
			throw e;
		}
	}
}