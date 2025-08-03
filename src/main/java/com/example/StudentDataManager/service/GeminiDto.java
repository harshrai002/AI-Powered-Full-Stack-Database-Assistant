package com.example.StudentDataManager.service;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// Request DTO
public class GeminiDto {

    // Request Models
    public record GeminiRequestPart(String text) {}

    public record GeminiRequestContent(List<GeminiRequestPart> parts) {}

    public record GeminiRequestBody(List<GeminiRequestContent> contents) {}

    // Response Models
    public record GeminiResponseBody(List<GeminiCandidate> candidates) {}

    public record GeminiCandidate(GeminiResponseContent content) {}

    public record GeminiResponseContent(List<GeminiResponsePart> parts) {}

    public record GeminiResponsePart(@JsonProperty("text") String text) {}
}
