package com.janondra.mdknowledgebase.folder.model;

import org.jspecify.annotations.Nullable;

import java.util.UUID;

public record Folder(
    UUID id,
    UUID ownerId,
    @Nullable UUID parentId,
    String folderName
) {}
