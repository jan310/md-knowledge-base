package com.janondra.mdknowledgebase.document.controller;

import com.janondra.mdknowledgebase.document.controller.dto.CreateDocumentDTO;
import com.janondra.mdknowledgebase.document.controller.dto.ResponseDocumentDTO;
import com.janondra.mdknowledgebase.document.controller.dto.UpdateDocumentContentDTO;
import com.janondra.mdknowledgebase.document.controller.dto.UpdateDocumentFilenameDTO;
import com.janondra.mdknowledgebase.document.controller.dto.UpdateDocumentTagsDTO;
import com.janondra.mdknowledgebase.document.mapper.DocumentMapper;
import com.janondra.mdknowledgebase.document.model.DocumentRef;
import com.janondra.mdknowledgebase.document.resolver.UserId;
import com.janondra.mdknowledgebase.document.service.DocumentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/documents")
@Validated
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public UUID createDocument(@UserId UUID userId, @RequestBody @Valid CreateDocumentDTO createDocumentDTO) {
        return documentService.saveDocument(
            DocumentMapper.toCreateDocument(userId, createDocumentDTO)
        );
    }

    @GetMapping("/{id}")
    @ResponseStatus(OK)
    public ResponseDocumentDTO getDocument(@PathVariable UUID id, @UserId UUID userId) {
        return DocumentMapper.toResponseDocumentDTO(
            documentService.getDocument(id, userId)
        );
    }

    @GetMapping
    @ResponseStatus(OK)
    public List<DocumentRef> getDocuments(
        @UserId UUID userId,
        @RequestParam(required = false) List<String> tags,
        @RequestParam(required = false) String searchText,
        @RequestParam(defaultValue = "20") @Max(200) int pageSize,
        @RequestParam(required = false) String lastFileName, // Keyset Pagination for search by userId and userId + tags
        @RequestParam(defaultValue = "0") int offset         // Offset Pagination for search by userId + search text
    ) {
        if (tags != null && !tags.isEmpty()) {
            // Get documents by tags
            return documentService.getDocuments(userId, tags, pageSize, lastFileName);
        } else if (searchText != null && !searchText.isBlank()) {
            // Get documents by searchText
            return documentService.getDocuments(userId, searchText, pageSize, offset);
        } else {
            // Get all documents
            return documentService.getDocuments(userId, pageSize, lastFileName);
        }
    }

    @PatchMapping("/{id}/filename")
    @ResponseStatus(NO_CONTENT)
    public void updateDocumentFilename(
        @PathVariable UUID id,
        @UserId UUID userId,
        @RequestBody UpdateDocumentFilenameDTO newFileName
    ) {
        documentService.updateDocumentFilename(id, userId, newFileName.filename());
    }

    @PatchMapping("/{id}/tags")
    @ResponseStatus(NO_CONTENT)
    public void updateDocumentTags(
        @PathVariable UUID id,
        @UserId UUID userId,
        @RequestBody UpdateDocumentTagsDTO newTags
    ) {
        documentService.updateDocumentTags(id, userId, newTags.tags());
    }

    @PatchMapping("/{id}/content")
    @ResponseStatus(NO_CONTENT)
    public void updateDocumentContent(
        @PathVariable UUID id,
        @UserId UUID userId,
        @RequestBody UpdateDocumentContentDTO newContent
    ) {
        documentService.updateDocumentContentAndQuestions(id, userId, newContent.content());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public void deleteDocument(@PathVariable UUID id, @UserId UUID userId) {
        documentService.deleteDocument(id, userId);
    }

}
