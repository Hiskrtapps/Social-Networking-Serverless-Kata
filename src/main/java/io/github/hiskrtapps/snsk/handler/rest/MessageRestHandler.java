package io.github.hiskrtapps.snsk.handler.rest;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder.standard;

/**
 * Handler for REST requests to Lambda function.
 */
public abstract class MessageRestHandler<O> implements RequestHandler<Map<Object, Object>, Object> {

    /**
     * Main handler method providing common nehvior for all the incoming requests
     */
    public final Object handleRequest(final Map<Object, Object> input, final Context context) {
        context.getLogger().log("Input: " + input);
        final O output = execute(input);
        return new GatewayResponse(buildBody(output), buildHeaders(output), 200);
    }

    /**
     * main logic here
     */
    protected abstract O execute(Map<Object, Object> input);

    /**
     * build the body string to be returned
     */
    protected abstract String buildBody(O output);

    /**
     * build the headers to be returned
     */
    protected abstract Map<String, String> buildHeaders(O output);

    /**
     * @return the DynamoDBMapper instqnce for qll the subclasses
     */
    protected final DynamoDBMapper dynamoDB() {
        return new DynamoDBMapper(standard().build());
    }

    /**
     * POJO containing response object for API Gateway.
     */
    public static final class GatewayResponse {

        private final String body;

        private final Map<String, String> headers;

        private final int statusCode;

        private GatewayResponse(final String body, final Map<String, String> headers, final int statusCode) {
            this.statusCode = statusCode;
            this.body = body;
            this.headers = Collections.unmodifiableMap(new HashMap<>(headers));
        }

        public String getBody() {
            return body;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }

    /**
     * model items to be returned in the responses. It is a subset of the Message model class
     */
    public static final class ResultMessage {

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
