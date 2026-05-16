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

    public void updateDocumentFilename(UUID id, UUID ownerId, String newFileName) {
        documentRepository.updateDocumentFilename(id, ownerId, newFileName);
    }

    public void updateDocumentTags(UUID id, UUID ownerId, List<String> newTags) {
        documentRepository.updateDocumentTags(id, ownerId, newTags);
    }

    public void updateDocumentContentAndQuestions(UUID id, UUID ownerId, String newContent) {
        documentRepository.updateDocumentContentAndClearQuestions(id, ownerId, newContent);
        documentEnrichmentService.generateAndSaveQuestions(id, newContent);
    }

    public void deleteDocument(UUID id, UUID ownerId) {
        documentRepository.deleteDocument(id, ownerId);
    }

}
