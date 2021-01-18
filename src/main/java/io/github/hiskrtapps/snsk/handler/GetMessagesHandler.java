package io.github.hiskrtapps.snsk.handler;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.ScanResultPage;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import io.github.hiskrtapps.snsk.model.Message;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Integer.parseInt;
import static java.lang.String.join;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Handler for requests to Lambda function.
 */
public final class GetMessagesHandler extends MessageRestHandler<ScanResultPage<Message>> {

    private static final String LAST_EVALUATED_KEY_HEADER = "x-snsk-pagination.LastEvaluatedKey";

    private static final String PAGE_LIMIT_HEADER = "x-snsk-page-Limit";

    private static final int PAGE_LIMIT_DEFAULT = 10;

    @Override
    protected final ScanResultPage<Message> execute(final Map<Object, Object> input) {
        return dynamoDB().scanPage(Message.class, scanExpression(input));
    }

    private DynamoDBScanExpression scanExpression(final Map<Object, Object> input) {
        final DynamoDBScanExpression scanExpression = new DynamoDBScanExpression().withIndexName("MoreRecentsFirst");

        final int pageLimit = readPageLimit(input);
        if (pageLimit > 0) {
            scanExpression.withLimit(pageLimit);
        }

        final Map<String, AttributeValue> exclusiveStartKey = readExclusiveStartKey(input);
        if (exclusiveStartKey != null) {
            scanExpression.withExclusiveStartKey(exclusiveStartKey);
        }

        return scanExpression;
    }

    @Override
    protected final String buildBody(final ScanResultPage<Message> result) {
        final JSONArray ja = new JSONArray();
        for (final Message m : result.getResults()) {
            ja.put(new JSONObject(new ResultMessage(m.getId(), m.getUserId(), m.getCreatedAt(), m.getMessage())));
        }
        return ja.toString();
    }

    @Override
    protected final Map<String, String> buildHeaders(final ScanResultPage<Message> result) {
        final Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", APPLICATION_JSON_VALUE);
        if (result.getLastEvaluatedKey() != null) {
            String id = result.getLastEvaluatedKey().get("id").getS();
            String recentness = result.getLastEvaluatedKey().get("recentness").getN();
            String status = result.getLastEvaluatedKey().get("status").getS();
            headers.put(LAST_EVALUATED_KEY_HEADER, join(";", id, recentness, status));
        }
        return headers;
    }

    private Map<String, AttributeValue> readExclusiveStartKey(final Map<Object, Object> input) {
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

    private int readPageLimit(final Map<Object, Object> input) {
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
