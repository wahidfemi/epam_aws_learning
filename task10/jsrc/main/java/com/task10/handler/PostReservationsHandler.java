
package com.task10.handler;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.json.JSONObject;
import java.util.UUID;

public class PostReservationsHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {

        System.out.println("going to create reservations data to dynamodb table");
        String reservationsTableName = System.getenv("reservations_table");
        String region = System.getenv("REGION");
        String id = UUID.randomUUID().toString();
        DynamoDB dynamoDB = new DynamoDB(AmazonDynamoDBAsyncClientBuilder.standard().withRegion(region).build());
        Table table = dynamoDB.getTable(reservationsTableName);
        JSONObject reservationsData = new JSONObject(requestEvent.getBody());
        //saving data to dynamodb table
        Item item = new Item().withPrimaryKey("id", id)
                    .withNumber("tableNumber", Integer.parseInt(reservationsData.get("tableNumber").toString()))
                    .withString("clientName", reservationsData.get("clientName").toString())
                    .withString("phoneNumber", reservationsData.get("phoneNumber").toString())
                    .withString("date", reservationsData.get("date").toString())
                    .withString("slotTimeStart", reservationsData.get("slotTimeStart").toString())
                    .withString("slotTimeEnd", reservationsData.get("slotTimeEnd").toString());

        table.putItem(item);

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody(new JSONObject().put("reservationId", id).toString());
    }

}
