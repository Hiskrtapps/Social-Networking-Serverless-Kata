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

        //String exclusiveStartKey = new JSONObject(input).getJSONObject("headers").getString("x-LastEvaluatedKey");
        String index = new JSONObject(input).getJSONObject("headers").getString("x-index");


        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();

        /*
        ScanRequest scanRequest = new ScanRequest()
                .withTableName("awscodestar-claranet-snsk_Message")
                //.withLimit(2)
                .withProjectionExpression("userId, createdAt, message")
        ;

        ScanResult result = client.scan(scanRequest);

        JSONArray ja = new JSONArray();
        for (Map<String, AttributeValue> item : result.getItems()){
            ja.put(new JSONObject(item));
        }
*/

        DynamoDBMapper mapper = new DynamoDBMapper(client);

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withLimit(4)//.withIndexName(index)
                //.withExclusiveStartKey(Collections.singletonMap("id", new AttributeValue().withS(exclusiveStartKey)))
        ;



        ScanResultPage<Message> scanResult = mapper.scanPage(Message.class, scanExpression);

        JSONArray ja = new JSONArray();
        for (Message item : scanResult.getResults()) {
            ja.put(new JSONObject(item));
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        if (scanResult.getLastEvaluatedKey() != null) {
            headers.put("x-LastEvaluatedKey", scanResult.getLastEvaluatedKey().get("id").getS());
        }


        //scanExpression = new DynamoDBScanExpression()
        //        .withLimit(2)
        //        .withExclusiveStartKey(scanResult.getLastEvaluatedKey());
        //scanResult = mapper.scanPage(Message.class, scanExpression);


        return new GatewayResponse(ja.toString(), headers, 200);
    }
}
