package com.janondra.mdknowledgebase.document.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Schema;
import com.janondra.mdknowledgebase.document.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DocumentEnrichmentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentEnrichmentService.class);

    private final String genAiModel;
    private final String promptTemplate;
    private final Client genAiClient;
    private final DocumentRepository documentRepository;
    private final JsonMapper jsonMapper;
    private final GenerateContentConfig genAiResponseConfig = createGenAiResponseConfig();

    public DocumentEnrichmentService(
        @Value("${google-gemini.model}") String genAiModel,
        @Value("classpath:prompt-template.txt") Resource promptTemplate,
        Client genAiClient,
        DocumentRepository documentRepository,
        JsonMapper jsonMapper
    ) throws IOException {
        this.genAiModel = genAiModel;
        this.promptTemplate = promptTemplate.getContentAsString(StandardCharsets.UTF_8);
        this.genAiClient = genAiClient;
        this.documentRepository = documentRepository;
        this.jsonMapper = jsonMapper;
    }

    @Async
    public void generateAndSaveQuestions(UUID documentId, String documentContent) {
        try {
            GenerateContentResponse genAiResponse = genAiClient.models.generateContent(
                genAiModel,
                promptTemplate + documentContent,
                genAiResponseConfig
            );

            String genAiResponseContent = genAiResponse.text();
            if (genAiResponseContent == null || genAiResponseContent.isBlank()) {
                log.warn("AI provided an empty response for document {}", documentId);
                return;
            }

            List<String> questions = jsonMapper.readValue(genAiResponseContent, QuestionsWrapper.class).questions();

            if (questions == null || questions.isEmpty()) {
                log.warn("AI returned an empty list of questions for document {}", documentId);
                return;
            }

            documentRepository.updateDocumentQuestions(documentId, questions);
        } catch (Exception e) {
            log.error("Error occurred while generating questions for document {}: {}", documentId, e.getMessage(), e);
        }
    }

    private GenerateContentConfig createGenAiResponseConfig() {
        return GenerateContentConfig.builder()
            .responseMimeType("application/json")
            .responseSchema(
                Schema.builder()
                    .type("OBJECT")
                    .properties(
                        Map.of(
                            "questions",
                            Schema.builder().type("ARRAY").items(
                                Schema.builder().type("STRING").build()
                            ).build()
                        )
                    )
                    .required("questions")
                    .build()
            )
            .build();
    }

}
