package com.janondra.mdknowledgebase.document.mapper;

import com.janondra.mdknowledgebase.document.controller.dto.CreateDocumentDTO;
import com.janondra.mdknowledgebase.document.model.CreateDocument;

import java.util.UUID;

public class DocumentMapper {

    public static CreateDocument toCreateDocument(UUID ownerId, CreateDocumentDTO createDocumentDTO) {
        return new CreateDocument(
            ownerId,
            createDocumentDTO.fileName(),
            createDocumentDTO.tags(),
            createDocumentDTO.content()
        );
    }

}
