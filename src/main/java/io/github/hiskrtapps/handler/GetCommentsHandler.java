package io.github.hiskrtapps.handler;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.ScanResultPage;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.ScanFilter;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.aws.codestar.projecttemplates.GatewayResponse;
import io.github.hiskrtapps.model.Message;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Attr;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

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
            /*
            JSONObject jExclusiveStartKey = new JSONObject(jInput.getJSONObject("headers").getString("x-LastEvaluatedKey"));
            exclusiveStartKey = Map.of(
                    "id", new AttributeValue().withS(jExclusiveStartKey.getJSONObject("id").getString("s")),
                    "recentness", new AttributeValue().withN(jExclusiveStartKey.getJSONObject("recentness").getString("n")),
                    "status", new AttributeValue().withS(jExclusiveStartKey.getJSONObject("status").getString("s"))

            );
            */
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
            headers.put("x-LastEvaluatedKey", new JSONObject(scanResult.getLastEvaluatedKey()).toString());
        }

        return new GatewayResponse(jInput.toString(), headers, 200);
        //return new GatewayResponse(ja.toString(), headers, 200);
    }
}
