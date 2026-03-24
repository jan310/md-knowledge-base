package com.janondra.mdknowledgebase.user.controller.dto;

import com.janondra.mdknowledgebase.user.controller.validation.email.ValidEmail;
import com.janondra.mdknowledgebase.user.controller.validation.timezone.ValidTimeZone;

public record CreateUserDTO(
    @ValidEmail String email,
    @ValidTimeZone String timeZone
) {}
