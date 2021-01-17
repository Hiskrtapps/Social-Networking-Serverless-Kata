package io.github.hiskrtapps.snsk.handler;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import io.github.hiskrtapps.snsk.infrastructure.GatewayResponse;
import io.github.hiskrtapps.snsk.model.Message;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder.standard;
import static java.lang.Long.MAX_VALUE;
import static java.time.LocalDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

/**
 * Handler for requests to Lambda function.
 */
public class PostMessageHandler implements RequestHandler<Map<Object, Object>, Object> {

    public Object handleRequest(final Map<Object, Object> input, final Context context) {
        context.getLogger().log("Input: " + input);
        saveMessage(buildMessage(input));
        return response();
    }

    private Object response() {
        final Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return new GatewayResponse("{\"result\" : \"ok\"}", headers, 200);
    }

    private void saveMessage(final Message message) {
        new DynamoDBMapper(standard().build()).save(message);
    }

    private Message buildMessage(final Map<Object, Object> input) {
        final JSONObject jInput = new JSONObject(input);
        final LocalDateTime now = now();
        long recentness = ZonedDateTime.of(now, systemDefault()).toInstant().toEpochMilli();
        final Message message = new Message();
        message.setRecentness(MAX_VALUE - recentness);
        message.setMessage(new JSONObject(jInput.getString("body")).getString("message"));
        message.setCreatedAt(ISO_DATE_TIME.format(now));
        message.setUserId(retrieveUserId(jInput.getJSONObject("headers").getString("authorization")));
        message.setStatus("OK");
        return message;
    }

    private String retrieveUserId(final String token) {
        return new JSONObject(new String(new Base64(true).decode(token.split("\\.")[1]))).getString("email");
    }

}
