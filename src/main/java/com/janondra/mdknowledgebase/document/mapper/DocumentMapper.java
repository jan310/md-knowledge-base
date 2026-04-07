package com.janondra.mdknowledgebase.document.mapper;

import com.janondra.mdknowledgebase.document.controller.dto.CreateDocumentDTO;
import com.janondra.mdknowledgebase.document.controller.dto.ResponseDocumentDTO;
import com.janondra.mdknowledgebase.document.model.CreateDocument;
import com.janondra.mdknowledgebase.document.model.Document;

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

    public static ResponseDocumentDTO toResponseDocumentDTO(Document document) {
        return new ResponseDocumentDTO(
            document.id(),
            document.fileName(),
            document.tags(),
            document.content()
        );
    }

}
