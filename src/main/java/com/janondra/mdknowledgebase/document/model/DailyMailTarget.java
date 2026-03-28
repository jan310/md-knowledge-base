package com.janondra.mdknowledgebase.document.model;

import java.util.List;

public record DailyMailTarget(
    String email,
    String fileName,
    String content,
    List<String> questions
) {}
