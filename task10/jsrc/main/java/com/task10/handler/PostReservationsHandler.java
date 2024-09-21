
package com.task10.handler;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;

public class PostReservationsHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {

        System.out.println("going to create reservations data to dynamodb table");
        String reservationsTableName = System.getenv("reservations_table");
        String region = System.getenv("REGION");

        JSONObject reservationsData = new JSONObject(requestEvent.getBody());

        String id = UUID.randomUUID().toString();

        //reading tables table data to check whether reservation table # exists or not
        String tablesTableName = System.getenv("tables_table");
        AmazonDynamoDBAsync dynamoDBClient = AmazonDynamoDBAsyncClientBuilder.standard().withRegion(region).build();
        ScanRequest scanRequest = new ScanRequest()
                .withTableName(tablesTableName);
        ScanResult result = dynamoDBClient.scan(scanRequest);
        boolean tableExists = false;
        System.out.println("reservation table number : "+reservationsData.get("tableNumber").toString());
        for (Map<String, AttributeValue> item : result.getItems()) {
            System.out.println("existing table id : "+item.get("id").getN());
            if(item.get("id").getN().equalsIgnoreCase(reservationsData.get("tableNumber").toString())){
                tableExists = true;
            }
        }

        if(tableExists) {
            DynamoDB dynamoDB = new DynamoDB(dynamoDBClient);
            Table table = dynamoDB.getTable(reservationsTableName);

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
        else {
            throw new NullPointerException();
        }
    }

}
