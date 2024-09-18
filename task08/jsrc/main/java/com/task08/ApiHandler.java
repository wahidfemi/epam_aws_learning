package com.task08;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import com.weather.WeatherData;
import org.json.simple.JSONObject;

@LambdaHandler(
    lambdaName = "api_handler",
	roleName = "api_handler-role",
	layers = {"sdk-layer"},
	runtime = DeploymentRuntime.JAVA17,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
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

public class ApiHandler implements RequestHandler<Object, JSONObject> {

	public JSONObject handleRequest(Object request, Context context) {
		System.out.println("Hello from lambda");
		WeatherData weatherData = new WeatherData();
        return weatherData.getWeatherData();
	}
}
