package io.github.hiskrtapps.snsk.handler;

import io.github.hiskrtapps.snsk.model.Message;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Long.MAX_VALUE;
import static java.time.LocalDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.time.ZonedDateTime.of;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Handler for requests to Lambda function.
 */
public final class PostMessageHandler extends MessageRestHandler<Message> {

    @Override
    protected final Message execute(final Map<Object, Object> input) {
        final Message message = buildMessage(input);
        dynamoDB().save(message);
        return message;
    }

    @Override
    protected final String buildBody(final Message m) {
        return new JSONObject(new ResultMessage(m.getId(), m.getUserId(), m.getCreatedAt(), m.getMessage())).toString();
    }

    @Override
    protected final Map<String, String> buildHeaders(final Message result) {
        final Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", APPLICATION_JSON_VALUE);
        return headers;
    }

    private Message buildMessage(final Map<Object, Object> input) {
        final JSONObject jInput = new JSONObject(input);
        final LocalDateTime now = now();
        long recentness = of(now, systemDefault()).toInstant().toEpochMilli();
        final Message message = new Message();
        message.setRecentness(MAX_VALUE - recentness);
        message.setMessage(new JSONObject(jInput.getString("body")).getString("message"));
        message.setCreatedAt(ISO_DATE_TIME.format(now));
        message.setUserId(retrieveUserId(jInput.getJSONObject("headers").getString("Authorization")));
        message.setStatus("OK");
        return message;
    }

    private String retrieveUserId(final String token) {
        return new JSONObject(new String(new Base64(true).decode(token.split("\\.")[1]))).getString("email");
    }

}
