package com.janondra.mdknowledgebase.document.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateDocumentTagsDTO(@Size(min = 1, max = 5) List<@NotBlank @Size(max = 50) String> tags) {}
