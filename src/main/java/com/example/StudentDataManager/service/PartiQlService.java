package com.example.StudentDataManager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ExecuteStatementRequest;
import software.amazon.awssdk.services.dynamodb.model.ExecuteStatementResponse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PartiQlService {

    private static final Logger logger = LoggerFactory.getLogger(PartiQlService.class);
    private final DynamoDbClient dynamoDbClient;

    public PartiQlService(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    /**
     * Executes a PartiQL query string against DynamoDB.
     * @param partiQlStatement The query string, likely from GeminiService.
     * @return A user-friendly formatted string of the results, or an error message.
     */
    public String executePartiQl(String partiQlStatement) {
        // Defensive check: Don't try to execute an error message from the previous step.
        if (partiQlStatement == null || partiQlStatement.trim().isEmpty() || partiQlStatement.startsWith("Error:")) {
            logger.warn("Skipping execution of invalid or error-producing statement: {}", partiQlStatement);
            return partiQlStatement;
        }

        try {
            logger.info("Executing PartiQL statement: {}", partiQlStatement);
            ExecuteStatementRequest request = ExecuteStatementRequest.builder()
                    .statement(partiQlStatement)
                    .build();

            ExecuteStatementResponse response = dynamoDbClient.executeStatement(request);
            List<Map<String, AttributeValue>> items = response.items();

            if (items.isEmpty()) {
                return "No students found matching your query.";
            }

            // Convert the raw DynamoDB items into a readable string for the UI
            return items.stream()
                    .map(this::convertDynamoDbItemToString)
                    .collect(Collectors.joining("\n---\n"));

        } catch (DynamoDbException e) {
            logger.error("Failed to execute PartiQL statement: '{}'. AWS Error: {}", partiQlStatement, e.awsErrorDetails().errorMessage(), e);
            return "There was an error executing your query. Please check the syntax or try again. Details: " + e.awsErrorDetails().errorMessage();
        } catch (Exception e) {
            logger.error("An unexpected error occurred while executing PartiQL statement: {}", partiQlStatement, e);
            return "An unexpected error occurred. Please contact support.";
        }
    }

    /**
     * Converts a raw DynamoDB item into a formatted, human-readable string.
     * @param item A map representing a single DynamoDB item.
     * @return A formatted string.
     */
    private String convertDynamoDbItemToString(Map<String, AttributeValue> item) {
        // Safely extract attributes, providing defaults if they don't exist to prevent errors.

        return item.entrySet().stream()
                .map(entry -> {
                    String key = entry.getKey();
                    AttributeValue value = entry.getValue();
                    String stringValue;

                    // Handle the different data types that can be returned
                    if (value.s() != null) {
                        stringValue = value.s();
                    } else if (value.n() != null) {
                        stringValue = value.n();
                    } else if (value.bool() != null) {
                        stringValue = value.bool().toString();
                    } else if (value.hasM()) {
                        // If a map is returned (like the full marks object), format it
                        stringValue = "{" + convertDynamoDbItemToString(value.m()) + "}";
                    } else {
                        stringValue = "[unsupported type]";
                    }
                    return key + ": " + stringValue;
                })
                .collect(Collectors.joining(", "));
    }
}
