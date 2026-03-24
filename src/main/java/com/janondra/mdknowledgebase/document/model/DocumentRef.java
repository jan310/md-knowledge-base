package com.janondra.mdknowledgebase.document.model;

import java.util.UUID;

public record DocumentRef(
    UUID id,
    String fileName
) {}
