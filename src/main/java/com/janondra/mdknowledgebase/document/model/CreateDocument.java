package com.janondra.mdknowledgebase.document.model;

import java.util.List;
import java.util.UUID;

public record CreateDocument(
    UUID ownerId,
    String fileName,
    List<String> tags,
    String content,
    List<String> questions
) {}
