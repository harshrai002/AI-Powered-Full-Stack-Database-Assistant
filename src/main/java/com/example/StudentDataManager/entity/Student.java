package com.example.StudentDataManager.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Map;

@Setter
@Getter
@DynamoDBTable(tableName = "student-data")
public class Student {

    @DynamoDBHashKey
    @DynamoDBAttribute
    private Long id;

    @DynamoDBAttribute
    private String name;

    @DynamoDBAttribute
    private String email;

    @DynamoDBAttribute
    private Map<String, Integer> marks;

    // Getters and Setters
}
