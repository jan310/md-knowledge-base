package com.janondra.mdknowledgebase.document.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateDocumentDTO(
    @NotBlank String fileName,
    @Size(min = 1, max = 5) List<@NotBlank String> tags,
    @NotBlank String content
) {}
