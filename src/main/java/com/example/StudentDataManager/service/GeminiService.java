package com.example.StudentDataManager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class GeminiService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);


    private final String apiKey;
    private final RestTemplate restTemplate;

    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    public GeminiService(RestTemplate restTemplate, @Value("${gemini.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
    }


    public String getGeminiResponse(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Goog-Api-Key", apiKey);

        // Build request body using a type-safe DTO
        var requestPart = new GeminiDto.GeminiRequestPart(prompt);
        var requestContent = new GeminiDto.GeminiRequestContent(List.of(requestPart));
        var requestBody = new GeminiDto.GeminiRequestBody(List.of(requestContent));

        HttpEntity<GeminiDto.GeminiRequestBody> entity = new HttpEntity<>(requestBody, headers);

        try {
            // Let RestTemplate deserialize directly into DTO
            GeminiDto.GeminiResponseBody response = restTemplate.postForObject(GEMINI_API_URL, entity, GeminiDto.GeminiResponseBody.class);

            // Safely extract the text using Optional to prevent NullPointerExceptions
            return Optional.ofNullable(response)
                    .map(GeminiDto.GeminiResponseBody::candidates)
                    .filter(candidates -> !candidates.isEmpty())
                    .map(candidates -> candidates.get(0).content())
                    .map(GeminiDto.GeminiResponseContent::parts)
                    .filter(parts -> !parts.isEmpty())
                    .map(parts -> parts.get(0).text())
                    .map(String::trim)
                    .orElse("Sorry, I received an empty response from the AI service.");

        } catch (RestClientException e) {
            logger.error("Error calling Gemini API: {}", e.getMessage(), e);
            return "Sorry, there was an error communicating with the AI service.";
        }
    }
}
