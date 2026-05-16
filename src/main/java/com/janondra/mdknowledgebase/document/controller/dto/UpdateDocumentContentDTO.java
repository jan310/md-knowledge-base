package com.janondra.mdknowledgebase.document.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateDocumentContentDTO(@NotBlank @Size(max = 50_000) String content) {}
