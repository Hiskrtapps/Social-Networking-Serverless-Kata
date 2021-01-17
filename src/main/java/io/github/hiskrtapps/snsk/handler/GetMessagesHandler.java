package io.github.hiskrtapps.snsk.handler;

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

import static com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder.standard;
import static java.lang.Integer.parseInt;
import static java.lang.String.join;

/**
 * Handler for requests to Lambda function.
 */
public class GetMessagesHandler implements RequestHandler<Map<Object, Object>, Object> {

    private static final String LAST_EVALUATED_KEY_HEADER = "x-snsk-pagination.LastEvaluatedKey";

    private static final String PAGE_LIMIT_HEADER = "x-snsk-page-limit";

    private static final int PAGE_LIMIT_DEFAULT = 10;

    public Object handleRequest(final Map<Object, Object> input, final Context context) {
        context.getLogger().log("Input: " + input);
        final ScanResultPage<Message> result = new DynamoDBMapper(standard().build()).scanPage(Message.class, buildScanExpression(input));
        return new GatewayResponse(buildBody(result), buildHeaders(result.getLastEvaluatedKey()), 200);
    }

    private String buildBody(ScanResultPage<Message> result) {
        final JSONArray ja = new JSONArray();
        for (final Message m : result.getResults()) {
            final Message responseMessage = new Message(m.getId(), null, m.getUserId(), m.getCreatedAt(), m.getMessage(), null);
            ja.put(new JSONObject(responseMessage));
        }
        return ja.toString();
    }

    private Map<String, String> buildHeaders(Map<String, AttributeValue> lastEvaluatedKey) {
        final Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        if (lastEvaluatedKey != null) {
            final String id = lastEvaluatedKey.get("id").getS();
            final String recentness = lastEvaluatedKey.get("recentness").getN();
            final String status = lastEvaluatedKey.get("status").getS();
            headers.put(LAST_EVALUATED_KEY_HEADER, join(";", id, recentness, status));
        }
        return headers;
    }

    private DynamoDBScanExpression buildScanExpression(Map<Object, Object> input) {
        final Map<String, AttributeValue> exclusiveStartKey = readExclusiveStartKey(input);
        final int pageLimit = readPageLimit(input);
        final DynamoDBScanExpression scanExpression = new DynamoDBScanExpression().withIndexName("MoreRecentsFirst");
        if (pageLimit > 0) {
            scanExpression.withLimit(pageLimit);
        }
        if (exclusiveStartKey != null) {
            scanExpression.withExclusiveStartKey(exclusiveStartKey);
        }
        return scanExpression;
    }

    private Map<String, AttributeValue> readExclusiveStartKey(Map<Object, Object> input) {
        final JSONObject jInput = new JSONObject(input);
        if (!jInput.isNull("headers")) {
            final JSONObject headers = jInput.getJSONObject("headers");
            if (!headers.isNull(LAST_EVALUATED_KEY_HEADER)) {
                String[] tokens = headers.getString(LAST_EVALUATED_KEY_HEADER).split(";");
                return Map.of(
                        "id", new AttributeValue().withS(tokens[0]),
                        "recentness", new AttributeValue().withN(tokens[1]),
                        "status", new AttributeValue().withS(tokens[2])
                );
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private int readPageLimit(Map<Object, Object> input) {
        final JSONObject jInput = new JSONObject(input);
        if (!jInput.isNull("headers")) {
            final JSONObject headers = jInput.getJSONObject("headers");
            if (!headers.isNull(PAGE_LIMIT_HEADER)) {
                return parseInt(headers.getString(PAGE_LIMIT_HEADER));
            } else {
                return PAGE_LIMIT_DEFAULT;
            }
        } else {
            return PAGE_LIMIT_DEFAULT;
        }
    }
}
