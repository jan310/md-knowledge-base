package com.janondra.mdknowledgebase.document.model;

import java.util.UUID;

public record DocumentContent(
    UUID id,
    String content
) {}
