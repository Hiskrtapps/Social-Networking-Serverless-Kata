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
import static java.lang.String.join;

/**
 * Handler for requests to Lambda function.
 */
public class GetCommentsHandler implements RequestHandler<Map<Object, Object>, Object> {

    private static final String LAST_EVALUATED_KEY_HEADER = "x-snsk-pagination.LastEvaluatedKey";

    private static final String PAGINATION_DISABLED_HEADER = "x-snsk-pagination-disabled";

    private static final int PAGE_LIMIT = 10;

    public Object handleRequest(final Map<Object, Object> input, final Context context) {

        final ScanResultPage<Message> result = new DynamoDBMapper(standard().build()).scanPage(Message.class, buildScanExpression(input));

        final JSONArray ja = new JSONArray();
        for (final Message item : result.getResults()) {
            ja.put(new JSONObject(item));
        }
        final Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        if (result.getLastEvaluatedKey() != null) {
            final String id = result.getLastEvaluatedKey().get("id").getS();
            final String recentness = result.getLastEvaluatedKey().get("recentness").getN();
            final String status = result.getLastEvaluatedKey().get("status").getS();
            headers.put("LAST_EVALUATED_KEY_HEADER", join(";", id, recentness, status));
        }
        return new GatewayResponse(ja.toString(), headers, 200);
    }

    private DynamoDBScanExpression buildScanExpression(Map<Object, Object> input) {
        final Map<String, AttributeValue> exclusiveStartKey = readExclusiveStartKey(input);
        final Boolean paginationDisabled = readPaginationDisabled(input);
        final DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withLimit(PAGE_LIMIT);
        if (!paginationDisabled) {
            scanExpression.withIndexName("MoreRecentsFirst");
        }
        if (exclusiveStartKey != null) {
            scanExpression.withExclusiveStartKey(exclusiveStartKey);
        }
        return scanExpression;
    }

    private Map<String, AttributeValue> readExclusiveStartKey(Map<Object, Object> input) {
        final Map<String, AttributeValue> exclusiveStartKey;
        final JSONObject jInput = new JSONObject(input);
        if (!jInput.isNull("headers")) {
            final JSONObject headers = jInput.getJSONObject("headers");
            if (!headers.isNull("LAST_EVALUATED_KEY_HEADER")) {
                String[] tokens = headers.getString(LAST_EVALUATED_KEY_HEADER).split(";");
                exclusiveStartKey = Map.of(
                        "id", new AttributeValue().withS(tokens[0]),
                        "recentness", new AttributeValue().withN(tokens[1]),
                        "status", new AttributeValue().withS(tokens[2])
                );
            } else {
                exclusiveStartKey = null;
            }
        } else {
            exclusiveStartKey = null;
        }
        return exclusiveStartKey;
    }

    private boolean readPaginationDisabled(Map<Object, Object> input) {
        final boolean paginationDisabled;
        final JSONObject jInput = new JSONObject(input);
        if (!jInput.isNull("headers")) {
            final JSONObject headers = jInput.getJSONObject("headers");
            if (!headers.isNull("PAGINATION_DISABLED_HEADER")) {
                paginationDisabled = Boolean.parseBoolean(headers.getString(PAGINATION_DISABLED_HEADER));
            } else {
                paginationDisabled = false;
            }
        } else {
            paginationDisabled = false;
        }
        return paginationDisabled;
    }
}
