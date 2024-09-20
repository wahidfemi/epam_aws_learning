
package com.task10.handler;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.json.JSONObject;

import java.time.Instant;
import java.util.UUID;

public class PostTablesHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {

        System.out.println("going to create table data to dynamodb table");
        String tablesTableName = System.getenv("tables_table");
        String region = System.getenv("REGION");
        String id = UUID.randomUUID().toString();
        String createdAt = Instant.now().toString();
        DynamoDB dynamoDB = new DynamoDB(AmazonDynamoDBAsyncClientBuilder.standard().withRegion(region).build());
        Table table = dynamoDB.getTable(tablesTableName);

        JSONObject tablesData = new JSONObject(requestEvent.getBody());
        int tableId = Integer.parseInt(tablesData.get("id").toString());
        //saving data to dynamodb table
        Item item = new Item().withPrimaryKey("id", tableId)
                    .withNumber("number", Integer.parseInt(tablesData.get("number").toString()))
                    .withNumber("places", Integer.parseInt(tablesData.get("places").toString()))
                    .withBoolean("isVip", Boolean.parseBoolean(tablesData.get("isVip").toString()))
                    .withNumber("minOrder", Integer.parseInt(tablesData.get("minOrder").toString()));

        PutItemOutcome outcome = table.putItem(item);

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody(new JSONObject().put("id", tableId).toString());
    }

}
