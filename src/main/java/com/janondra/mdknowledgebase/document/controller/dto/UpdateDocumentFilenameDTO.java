package com.janondra.mdknowledgebase.document.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateDocumentFilenameDTO(@NotBlank @Size(max = 100) String filename) {}
