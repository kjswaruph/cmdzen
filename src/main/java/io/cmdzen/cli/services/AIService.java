package io.cmdzen.cli.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.cmdzen.cli.config.AIConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class AIService {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final AIConfig aiConfig;
    private final RestClient restClient;
    private final String doAgentKey;
    private final boolean doAgentEnabled;

    public AIService(AIConfig aiConfig,
                     RestClient.Builder restClientBuilder,
                     @Value("${do.agent.url:}") String doAgentUrl,
                     @Value("${do.agent.key:}") String doAgentKey) {
        this.aiConfig = aiConfig;
        this.doAgentKey = doAgentKey;
        this.doAgentEnabled = doAgentUrl != null && !doAgentUrl.isEmpty() &&
                              doAgentKey != null && !doAgentKey.isEmpty();

        if (doAgentEnabled) {
            this.restClient = restClientBuilder.baseUrl(doAgentUrl).build();
            log.info("DigitalOcean Agent enabled at: {}", doAgentUrl);
        } else {
            this.restClient = restClientBuilder.build();
            log.info("DigitalOcean Agent disabled, using OpenRouter only");
        }
    }

    public String ask(String prompt) {

        if (doAgentEnabled) {
            try {
                log.debug("Attempting to use DigitalOcean Agent");
                String response = getFromDOAgent(prompt);
                if (response != null && !response.isEmpty()) {
                    log.info("Successfully got response from DigitalOcean Agent");
                    return response;
                }
            } catch (Exception e) {
                log.warn("DigitalOcean Agent failed, falling back to OpenRouter: {}", e.getMessage());
            }
        }

        log.debug("Using OpenRouter API");
        return askOpenRouter(prompt);
    }

    private String getFromDOAgent(String prompt) {
        try {
            ObjectNode body = mapper.createObjectNode();
            ArrayNode messages = body.putArray("messages");
            ObjectNode userMsg = messages.addObject();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);
            //body.put("temperature", 0.1);
            //body.put("top_p", 0.3);
            //body.put("max_completion_tokens", 200);
            body.put("stream", false);
            body.put("include_functions_info", false);
            body.put("include_retrieval_info", false);
            body.put("include_guardrails_info", false);
            String rawResponse = restClient.post()
                    .uri("/api/v1/chat/completions")
                    .header("Authorization", "Bearer " + doAgentKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            log.debug("DigitalOcean raw response: {}", rawResponse);
            JsonNode root = mapper.readTree(rawResponse);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                String content =  choices.get(0)
                        .path("message")
                        .path("content")
                        .asText(null);
                return sanitize(content);
            }
            return sanitize(rawResponse); // fallback raw dump
        } catch (Exception e) {
            log.error("DigitalOcean Agent error", e);
            throw new RuntimeException("D.O Agent error: " + e.getMessage(), e);
        }
    }

    private String sanitize(String content) {
        if (content == null) return null;
        content = content.replaceAll("(?s)<think>.*?</think>", "");
        return content.trim();
    }

    private String askOpenRouter(String prompt) {
        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("model", aiConfig.getModel());
            ArrayNode messages = body.putArray("messages");
            ObjectNode userMsg = messages.addObject();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);
            String endpoint = aiConfig.getEndpoint();
            log.debug("OpenRouter endpoint: {}", endpoint);
            String rawResponse = restClient.post()
                    .uri(endpoint)
                    .header("Authorization", "Bearer " + aiConfig.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            log.debug("OpenRouter raw response: {}", rawResponse);
            JsonNode root = mapper.readTree(rawResponse);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                return choices.get(0)
                        .path("message")
                        .path("content")
                        .asText();
            }
            return rawResponse; // fallback

        } catch (Exception e) {
            log.error("OpenRouter Service Error", e);
            return "AI Error: " + e.getMessage();
        }
    }

}
