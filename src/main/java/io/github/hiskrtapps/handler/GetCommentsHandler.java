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
public class GetCommentsHandler implements RequestHandler<Object, Object> {

    public Object handleRequest(final Object input, final Context context) {
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
                .withLimit(2)
                .withExclusiveStartKey(Collections.singletonMap("id", new AttributeValue().withS("")));

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
        //return new GatewayResponse(new JSONObject().put("Messages", ja).toString(), headers, 200);
        return new GatewayResponse(new JSONObject().put("Messages", input).toString(), headers, 200);

    }
}
