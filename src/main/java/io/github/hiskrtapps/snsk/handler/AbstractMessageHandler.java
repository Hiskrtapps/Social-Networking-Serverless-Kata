package io.github.hiskrtapps.snsk.handler;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.json.JSONObject;
import org.springframework.http.MediaType;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder.standard;
import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Handler for requests to Lambda function.
 */
public abstract class AbstractMessageHandler<O> implements RequestHandler<Map<Object, Object>, Object> {

    public Object handleRequest(final Map<Object, Object> input, final Context context) {
        context.getLogger().log("Input: " + input);
        final O output = execute(input);
        return new GatewayResponse(buildBody(output), buildHeaders(output), 200);
    }

    protected abstract O execute(Map<Object, Object> input);

    protected abstract String buildBody(O output);

    protected abstract Map<String, String> buildHeaders(O output);

    protected final DynamoDBMapper dynamoDB() {
        return new DynamoDBMapper(standard().build());
    }

    /**
     * POJO containing response object for API Gateway.
     */
    public static class GatewayResponse {

        private final String body;
        private final Map<String, String> headers;
        private final int statusCode;

        private GatewayResponse(final String body, final Map<String, String> headers, final int statusCode) {
            this.statusCode = statusCode;
            this.body = body;
            this.headers = Collections.unmodifiableMap(new HashMap<>(headers));
        }

        private String getBody() {
            return body;
        }

        private Map<String, String> getHeaders() {
            return headers;
        }

        private int getStatusCode() {
            return statusCode;
        }
    }

    public static class ResultMessage {

        private final String id;

        private final String userId;

        private final String createdAt;

        private final String message;

        public ResultMessage(String id, String userId, String createdAt, String message) {
            this.id = id;
            this.userId = userId;
            this.createdAt = createdAt;
            this.message = message;
        }

        public String getId() {
            return id;
        }

        public String getUserId() {
            return userId;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getMessage() {
            return message;
        }

    }

}
