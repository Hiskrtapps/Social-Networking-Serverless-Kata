package io.github.hiskrtapps.snsk.handler;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.ScanResultPage;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import io.github.hiskrtapps.snsk.infrastructure.GatewayResponse;
import io.github.hiskrtapps.snsk.model.Message;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static java.time.LocalDateTime.now;

/**
 * Handler for requests to Lambda function.
 */
public class GetCommentsHandler implements RequestHandler<Map<Object, Object>, Object> {

    public Object handleRequest(final Map<Object, Object> input, final Context context) {
        context.getLogger().log(String.format("Input: %s", input));
        context.getLogger().log(String.format("new JSONObject(input): %s", new JSONObject(input)));
        context.getLogger().log(String.format("new JSONObject().put(\"I\", input): %s", new JSONObject().put("I", input)));

        Map<String, AttributeValue> exclusiveStartKey = new HashMap<>();
        JSONObject jInput = new JSONObject(input);
        if (!jInput.isNull("headers")) {
            String[] s = jInput.getJSONObject("headers").getString("x-LastEvaluatedKey").split(";");
            exclusiveStartKey = Map.of(
                    "id", new AttributeValue().withS(s[0]),
                    "recentness", new AttributeValue().withN(s[1]),
                    "status", new AttributeValue().withS(s[2])

            );
        }

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();


        DynamoDBMapper mapper = new DynamoDBMapper(client);

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withLimit(4).withIndexName("MoreRecentsFirst");

        if (exclusiveStartKey != null && !exclusiveStartKey.isEmpty()) {
            scanExpression.withExclusiveStartKey(exclusiveStartKey);
        }

        ScanResultPage<Message> scanResult = mapper.scanPage(Message.class, scanExpression);

        JSONArray ja = new JSONArray();
        for (Message item : scanResult.getResults()) {
            ja.put(new JSONObject(item));
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        if (scanResult.getLastEvaluatedKey() != null) {
            String id = scanResult.getLastEvaluatedKey().get("id").getS();
            String recentness = scanResult.getLastEvaluatedKey().get("recentness").getN();
            String status = scanResult.getLastEvaluatedKey().get("status").getS();
            headers.put("x-LastEvaluatedKey", String.join(";", id, recentness, status));
        }

        return new GatewayResponse(ja.toString(), headers, 200);
    }
}
