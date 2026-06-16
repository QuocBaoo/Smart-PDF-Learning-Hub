package com.smartpdf.hub.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models}")
    private String apiUrl;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void init() {
        if (apiKey == null || apiKey.trim().isEmpty() || "YOUR_GEMINI_API_KEY".equals(apiKey)) {
            log.warn("Gemini API Key is not configured! AI functionalities will not work.");
        } else {
            log.info("Gemini API Service initialized successfully.");
        }
    }

    /**
     * Generates a 768-dimensional vector embedding for the given text.
     */
    public List<Double> getEmbedding(String text) {
        String url = String.format("%s/text-embedding-004:embedContent?key=%s", apiUrl, apiKey);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Build request body: {"model": "models/text-embedding-004", "content": {"parts": [{"text": "text"}]}}
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", "models/text-embedding-004");
        ObjectNode content = requestBody.putObject("content");
        ArrayNode parts = content.putArray("parts");
        ObjectNode part = parts.addObject();
        part.put("text", text);

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, entity, JsonNode.class);
            JsonNode body = response.getBody();
            if (body != null && body.has("embedding") && body.get("embedding").has("values")) {
                JsonNode valuesNode = body.get("embedding").get("values");
                List<Double> embedding = new ArrayList<>();
                for (JsonNode value : valuesNode) {
                    embedding.add(value.asDouble());
                }
                return embedding;
            }
            throw new RuntimeException("Invalid response structure from Gemini Embedding API");
        } catch (Exception e) {
            log.error("Failed to generate embedding from Gemini API for text: {}", text, e);
            throw new RuntimeException("Gemini API Embedding generation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Generates text content using gemini-1.5-flash with system instruction and prompt.
     */
    public String generateContent(String systemInstruction, String userPrompt) {
        String url = String.format("%s/gemini-1.5-flash:generateContent?key=%s", apiUrl, apiKey);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Build request body:
        // {
        //   "contents": [{"parts": [{"text": "userPrompt"}]}],
        //   "systemInstruction": {"parts": [{"text": "systemInstruction"}]}
        // }
        ObjectNode requestBody = objectMapper.createObjectNode();
        ArrayNode contents = requestBody.putArray("contents");
        ObjectNode userContent = contents.addObject();
        ArrayNode userParts = userContent.putArray("parts");
        ObjectNode userPart = userParts.addObject();
        userPart.put("text", userPrompt);

        if (systemInstruction != null && !systemInstruction.trim().isEmpty()) {
            ObjectNode systemInstructionNode = requestBody.putObject("systemInstruction");
            ArrayNode systemParts = systemInstructionNode.putArray("parts");
            ObjectNode systemPart = systemParts.addObject();
            systemPart.put("text", systemInstruction);
        }

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, entity, JsonNode.class);
            JsonNode body = response.getBody();
            if (body != null && body.has("candidates")) {
                JsonNode candidate = body.get("candidates").get(0);
                if (candidate.has("content") && candidate.get("content").has("parts")) {
                    return candidate.get("content").get("parts").get(0).get("text").asText();
                }
            }
            throw new RuntimeException("Invalid response structure from Gemini GenerateContent API");
        } catch (Exception e) {
            log.error("Failed to generate content from Gemini API", e);
            throw new RuntimeException("Gemini API Content generation failed: " + e.getMessage(), e);
        }
    }
}
