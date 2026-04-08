package com.janondra.mdknowledgebase.user.controller.dto;

import com.janondra.mdknowledgebase.user.controller.validation.timezone.ValidTimeZone;

public record CreateUserDTO(
    String email,
    @ValidTimeZone String timeZone
) {}
