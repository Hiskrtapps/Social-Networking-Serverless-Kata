package io.github.hiskrtapps.snsk.handler.rest;

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
 * Handler for GET requests to Lambda function.
 * <p>
 * the responsability of this class is to return the stored messages based on the input parameters.
 * This implementation provides pagination capabilities starting from the following parameters:
 *
 *
 * x-snsk-page-Limit: it is the maximum number of values per page that will be returned (the last page will containing only the remainng elements);
 * - if this header value is not passed the default value is used (10)
 * - if the value 0 is passed the pagination will be automatically disabled and all the elements are returned (DANGER!)
 *
 * x-snsk-pagination-LastEvaluatedKey: it is the key of the last element returned in a previous endpoint call in which the pagination was enabled
 * - if this header is not passed the selection start form the first element (the more recently inserted)
 * - if the value from a previous call is passed the selection start form the next element starting from the one referenced by the key
 */
public final class GetMessagesHandler extends MessageRestHandler<ScanResultPage<Message>> {

    private static final String LAST_EVALUATED_KEY_HEADER = "x-snsk-pagination-LastEvaluatedKey";

    private static final String PAGE_LIMIT_HEADER = "x-snsk-page-Limit";

    private static final int PAGE_LIMIT_DEFAULT = 10;

    @Override
    protected final ScanResultPage<Message> execute(final Map<Object, Object> input) {
        return dynamoDB().scanPage(Message.class, scanExpression(input));
    }

    /**
     * it create a scan expression to retrieve (and paginate) messages orderd by recentness
     */
    private DynamoDBScanExpression scanExpression(final Map<Object, Object> input) {

        // expression is created; the proper index is set to be used
        final DynamoDBScanExpression scanExpression = new DynamoDBScanExpression().withIndexName("MoreRecentsFirst");

        // read page limit from the header
        final int pageLimit = readPageLimit(input);
        if (pageLimit > 0) {
            scanExpression.withLimit(pageLimit);
        }

        // read the offset key to start the load of the next page from
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

    /**
     * read the start key from the header
     */
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

    /**
     * read the page limit from the header
     */
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
