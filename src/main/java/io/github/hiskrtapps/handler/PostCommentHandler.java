package io.github.hiskrtapps.handler;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.aws.codestar.projecttemplates.GatewayResponse;
import io.github.hiskrtapps.model.Message;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

/**
 * Handler for requests to Lambda function.
 */
public class PostCommentHandler implements RequestHandler<Map<Object, Object>, Object> {

    public Object handleRequest(final Map<Object, Object> input, final Context context) {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();

        DynamoDBMapper mapper = new DynamoDBMapper(client);

        LocalDateTime now = now();
        long recentness = ZonedDateTime.of(now, ZoneId.systemDefault()).toInstant().toEpochMilli();

        JSONObject body = new JSONObject(input);
        //String message = body.getString("message");
        String userId = body.getString("userId");

        /*
        DynamoDB dynamoDB = new DynamoDB(client);


        Table table = dynamoDB.getTable("awscodestar-claranet-snsk_Message");
        Item item = new Item()
                .withPrimaryKey("id", UUID.randomUUID(), "recentness", recentness)
                .withString("userId", userId)
                .withString("createdAt", ISO_DATE_TIME.format(now()))
                .withString("message", message);

        // Write the item to the table
        PutItemOutcome outcome = table.putItem(item);
        */

        Message message = new Message();
        message.setRecentness(recentness);
        message.setMessage(body.getString("message"));
        message.setCreatedAt(ISO_DATE_TIME.format(now()));
        message.setUserId(body.getString("userId"));

        mapper.save(message);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return new GatewayResponse(new JSONObject().put("Output", input).toString(), headers, 200);
    }

}
