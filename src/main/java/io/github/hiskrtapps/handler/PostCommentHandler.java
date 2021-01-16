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
        context.getLogger().log(String.format("Input: %s", input));
        context.getLogger().log(String.format("new JSONObject(input): %s", new JSONObject(input)));
        context.getLogger().log(String.format("new JSONObject().put(\"I\", input): %s", new JSONObject().put("I", input)));

        JSONObject body = new JSONObject(new JSONObject(input).getString("body"));

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();

        DynamoDBMapper mapper = new DynamoDBMapper(client);

        LocalDateTime now = now();
        long recentness = ZonedDateTime.of(now, ZoneId.systemDefault()).toInstant().toEpochMilli();

        //JSONObject body = new JSONObject(input);

        Message message = new Message();
        message.setRecentness(Long.MAX_VALUE - recentness);
        message.setMessage(body.getString("message"));
        message.setCreatedAt(ISO_DATE_TIME.format(now()));
        message.setUserId(body.getString("userId"));
        message.setStatus("OK");

        mapper.save(message);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return new GatewayResponse(new JSONObject().put("Output", input.toString()).toString(), headers, 200);
    }

}
