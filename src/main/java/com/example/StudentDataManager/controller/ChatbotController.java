package com.example.StudentDataManager.controller;
import com.example.StudentDataManager.service.GeminiService;
import com.example.StudentDataManager.service.PartiQlService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "http://localhost:3000")
public class ChatbotController {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotController.class);

    private final GeminiService geminiService;
    private final PartiQlService partiQlService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public ChatbotController(GeminiService geminiService, PartiQlService partiQlService) {
        this.geminiService = geminiService;
        this.partiQlService = partiQlService;
    }

    @PostMapping("/query")
    public Map<String, Object> handleQuery(@RequestBody Map<String, String> body) {
        String userMessage = body.get("message");

        // STEP 1: Get the PartiQL Query from the AI
        String queryGenPrompt = buildEnhancedPrompt(userMessage);
        String aiResponseJson = geminiService.getGeminiResponse(queryGenPrompt);

        if (aiResponseJson == null) {
            return Map.of("response", "The AI service is currently unavailable.", "type", "error");
        }

        try {
            String cleanedJson = aiResponseJson.replaceAll("(?s)```(json)?\\s*|\\s*```", "").trim();
            JsonNode responseJson = objectMapper.readTree(cleanedJson);
            String responseType = responseJson.get("responseType").asText();

            if ("info".equals(responseType)) {
                return Map.of("response", responseJson.get("responseMessage").asText(), "type", "info");
            }

            // STEP 2: Execute the Query to Get Raw Data
            String partiQlQuery = responseJson.get("responseMessage").asText();
            String rawDbResult = partiQlService.executePartiQl(partiQlQuery);

            if (rawDbResult.startsWith("No students") || rawDbResult.startsWith("There was an error")) {
                return Map.of("response", rawDbResult, "type", "info");
            }

            // STEP 3: Send Raw Data to AI for Final Formatting
            String formattingPrompt = buildFormattingPrompt(userMessage, rawDbResult);
            String finalAnswer = geminiService.getGeminiResponse(formattingPrompt);

            // Clean the final answer of any markdown
            finalAnswer = finalAnswer.replaceAll("(?s)```(json)?\\s*|\\s*```", "").trim();

            return Map.of("response", finalAnswer, "type", "finalResult");

        } catch (Exception e) {
            logger.error("Error processing request: " + e.getMessage(), e);
            return Map.of("response", "An error occurred while processing your request.", "type", "error");
        }
    }

    private String buildEnhancedPrompt(String userMessage) {
        return "System Prompt: You are an expert DynamoDB assistant for a student management system.\n\n" +

                "CRITICAL INSTRUCTIONS:\n" +
                "1. ALWAYS respond in valid JSON format with exactly these keys: `responseType` and `responseMessage`.\n" +
                "2. `responseType` can ONLY be 'sqlQuery' or 'info'.\n" +
                "3. If the user wants to query data, use 'sqlQuery'. For general questions ('who are you?'), use 'info'.\n\n" +

                "DATABASE SCHEMA:\n" +
                "Table: \"student-data\"\n" +
                "Primary Key: `id` (Number)\n" +
                "Attributes: `name` (String), `email` (String), `marks` (Map of String to Number).\n\n" +

                "SQL QUERY RULES:\n" +
                "- Use PartiQL syntax for DynamoDB.\n" +
                "- Always wrap table name in double quotes: \"student-data\".\n" +
                "- For nested attributes use dot notation, e.g., `marks.Maths`.\n" +
                "- Single line query only, no formatting.\n\n" +

                "CALCULATION AND MANIPULATION RULES:\n" +
                "- The user may ask for calculations (average, sum, count) or data manipulations (get first name, lowercase) that DynamoDB cannot do in a query.\n" +
                "- In these cases, your ONLY job is to generate a `sqlQuery` that fetches the necessary RAW DATA for the task.\n" +
                "- Example 1: If the user asks for 'average marks in Maths', you MUST return the query `SELECT marks.Maths FROM \"student-data\"`.\n" +
                "- Example 2: If the user asks for 'first names', you MUST return the query `SELECT name FROM \"student-data\"`.\n" +
                "- Do NOT respond that the function is not supported. Just provide the query to get the data so the application can do the calculation.\n\n" +

                "User Query: " + userMessage + "\n\n" +

                "Respond with valid JSON only:";
    }

    private String buildFormattingPrompt(String originalQuery, String rawData) {
        return "You are a helpful data formatting assistant. Your job is to answer the user's original question based on the raw data provided. " +
                "Format the answer clearly and concisely. Do not add any extra commentary or explanations. Just provide the direct answer.\n\n" +
                "Original User Question: \"" + originalQuery + "\"\n\n" +
                "Raw Data:\n" + rawData + "\n\n" +
                "Final Answer:";
    }
}

