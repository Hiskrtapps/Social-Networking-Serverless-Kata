package io.github.hiskrtapps.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGeneratedKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "awscodestar-claranet-snsk_Message")
public class Message {

    @DynamoDBAutoGeneratedKey
    @DynamoDBHashKey(attributeName = "id")
    private String id;

    @DynamoDBRangeKey(attributeName = "recentness")
    private Long recentness;

    @DynamoDBAttribute(attributeName = "userId")
    private String userId;

    @DynamoDBAttribute(attributeName = "createdAt")
    private String createdAt;

    @DynamoDBAttribute
    private String message;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getRecentness() {
        return recentness;
    }

    public void setRecentness(Long recentness) {
        this.recentness = recentness;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}