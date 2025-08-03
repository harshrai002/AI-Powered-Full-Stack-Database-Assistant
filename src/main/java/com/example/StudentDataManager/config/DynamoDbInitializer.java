package com.example.StudentDataManager.config;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceInUseException;
import com.example.StudentDataManager.entity.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DynamoDbInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DynamoDbInitializer.class);

    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    @Autowired
    private AmazonDynamoDB amazonDynamoDB;

    @Override
    public void run(String... args) throws Exception {
        // Create a CreateTableRequest from the Student entity
        CreateTableRequest createTableRequest = dynamoDBMapper.generateCreateTableRequest(Student.class);

        // Set the provisioned throughput for the table (required for table creation)
        // For DynamoDB Local, these values can be low.
        createTableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));

        try {
            // Attempt to create the table
            amazonDynamoDB.createTable(createTableRequest);
            logger.info("Successfully created table '{}'.", createTableRequest.getTableName());
        } catch (ResourceInUseException e) {
            // This exception means the table already exists, which is fine.
            logger.info("Table '{}' already exists. No action needed.", createTableRequest.getTableName());
        }
    }
}
