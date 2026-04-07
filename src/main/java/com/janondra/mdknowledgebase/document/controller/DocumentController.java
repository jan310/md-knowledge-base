package com.janondra.mdknowledgebase.document.controller;

import com.janondra.mdknowledgebase.document.controller.dto.CreateDocumentDTO;
import com.janondra.mdknowledgebase.document.mapper.DocumentMapper;
import com.janondra.mdknowledgebase.document.resolver.UserId;
import com.janondra.mdknowledgebase.document.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public UUID createDocument(
        @UserId UUID userId,
        @RequestBody @Valid CreateDocumentDTO createDocumentDTO
    ) {
        return documentService.saveDocument(
            DocumentMapper.toCreateDocument(
                userId,
                createDocumentDTO
            )
        );
    }

}
