package com.task05;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;

import java.util.HashMap;
import java.util.Map;

@LambdaHandler(
    lambdaName = "api_handler",
	roleName = "api_handler-role",
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
public class ApiHandler implements RequestHandler<ApiGatewayRequest, Map<String, Object>> {

	public Map<String, Object> handleRequest(ApiGatewayRequest apiRequest, Context context) {
		System.out.println("content from request : "+apiRequest.getContent());
		System.out.println("principal id  : "+apiRequest.getPrincipalId());
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("principalId", apiRequest.getPrincipalId());
		resultMap.put("content", apiRequest.getContent());
		return resultMap;
	}
}
