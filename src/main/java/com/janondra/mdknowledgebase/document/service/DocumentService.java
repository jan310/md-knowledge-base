package com.janondra.mdknowledgebase.document.service;

import com.janondra.mdknowledgebase.document.model.CreateDocument;
import com.janondra.mdknowledgebase.document.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentEnrichmentService documentEnrichmentService;

    public DocumentService(
        DocumentRepository documentRepository,
        DocumentEnrichmentService documentEnrichmentService
    ) {
        this.documentRepository = documentRepository;
        this.documentEnrichmentService = documentEnrichmentService;
    }

    public UUID saveDocument(CreateDocument createDocument) {
        var documentId = documentRepository.saveDocument(createDocument);
        documentEnrichmentService.generateAndSaveQuestions(documentId, createDocument.content());

        return documentId;
    }

}
