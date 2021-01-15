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
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler for requests to Lambda function.
 */
public class PostCommentHandler implements RequestHandler<Map<Object, Object>, Object> {

    public Object handleRequest(final Map<Object, Object> input, final Context context) {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        DynamoDB dynamoDB = new DynamoDB(client);

        System.out.println("input: " + input);
        System.out.println("input.get(\"body\"): " + input.get("body"));
        JSONObject body = new JSONObject(input.get("body"));
        System.out.println("body: " + body);
        String text = body.getString("text");
        System.out.println("text: " + text);
        String userId = body.getString("userId");
        System.out.println("userId: " + userId);

        Table table = dynamoDB.getTable("Messages");
        Item item = new Item()
                .withPrimaryKey("status", "OK")
                .withString("timestamp", LocalDate.now().format(DateTimeFormatter.ISO_INSTANT))
                .withString("text", text)
                .withString("userId", userId);

        // Write the item to the table
        PutItemOutcome outcome = table.putItem(item);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return new GatewayResponse(new JSONObject().put("Output", input.get("body")).toString(), headers, 200);
    }
}
