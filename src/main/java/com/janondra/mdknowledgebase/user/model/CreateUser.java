package com.janondra.mdknowledgebase.user.model;

public record CreateUser(
    String authId,
    String email,
    String timeZone
) {}
