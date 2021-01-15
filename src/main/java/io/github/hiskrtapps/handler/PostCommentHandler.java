package io.github.hiskrtapps.handler;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.aws.codestar.projecttemplates.GatewayResponse;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

/**
 * Handler for requests to Lambda function.
 */
public class PostCommentHandler implements RequestHandler<Map<Object, Object>, Object> {

    public Object handleRequest(final Map<Object, Object> input, final Context context) {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        DynamoDB dynamoDB = new DynamoDB(client);

        JSONObject body = new JSONObject(input);
        String message = body.getString("message");
        String userId = body.getString("userId");

        Table table = dynamoDB.getTable("awscodestar-claranet-snsk_Messages");
        Item item = new Item()
                .withPrimaryKey("status", "OK")
                .withString("createdAt", ISO_DATE_TIME.format(now()))
                .withString("message", message)
                .withString("userId", userId);

        // Write the item to the table
        PutItemOutcome outcome = table.putItem(item);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return new GatewayResponse(new JSONObject().put("Output", input).toString(), headers, 200);
    }

}
