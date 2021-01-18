package io.github.hiskrtapps.snsk.handler.stream;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.ScanResultPage;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import io.github.hiskrtapps.snsk.handler.rest.MessageRestHandler;
import io.github.hiskrtapps.snsk.model.Message;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Integer.parseInt;
import static java.lang.String.join;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Handler for event requests to Lambda function.
 */
public final class BackupMessagesHandler implements RequestHandler<Object, Object> {

    /**
     * not implemented; just logging the incoming events
     */
    @Override
    public Object handleRequest(Object input, Context context) {
        context.getLogger().log(input.toString());
        return null;
    }
}
