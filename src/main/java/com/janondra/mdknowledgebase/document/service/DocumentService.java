package com.janondra.mdknowledgebase.document.service;

import com.janondra.mdknowledgebase.document.model.CreateDocument;
import com.janondra.mdknowledgebase.document.model.Document;
import com.janondra.mdknowledgebase.document.model.DocumentRef;
import com.janondra.mdknowledgebase.document.repository.DocumentRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public Document getDocument(UUID id, UUID ownerId) {
        return documentRepository.getDocumentByIdAndOwnerId(id, ownerId);
    }

    public List<DocumentRef> getDocuments(UUID ownerId, int pageSize, @Nullable String lastFileName) {
        return documentRepository.getDocumentRefsByOwnerId(ownerId, pageSize, lastFileName);
    }

    public List<DocumentRef> getDocuments(UUID ownerId, List<String> tags, int pageSize, @Nullable String lastFileName) {
        return documentRepository.getDocumentRefsByOwnerIdAndTags(ownerId, tags, pageSize, lastFileName);
    }

    public List<DocumentRef> getDocuments(UUID ownerId, String searchText, int pageSize, int offset) {
        return documentRepository.getDocumentRefsByOwnerIdAndSearchText(ownerId, searchText, pageSize, offset);
    }

}
