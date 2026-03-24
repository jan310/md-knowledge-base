package com.janondra.mdknowledgebase.document.controller.dto;

import java.util.List;

public record CreateDocumentDTO(
    String fileName,
    List<String> tags,
    String content
) {}
