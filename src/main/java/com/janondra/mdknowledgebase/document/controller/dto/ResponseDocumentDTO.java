package com.janondra.mdknowledgebase.document.controller.dto;

import java.util.List;
import java.util.UUID;

public record ResponseDocumentDTO(
    UUID id,
    String fileName,
    List<String> tags,
    String content
) {}
