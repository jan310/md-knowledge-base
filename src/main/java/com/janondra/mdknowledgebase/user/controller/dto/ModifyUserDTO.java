package com.janondra.mdknowledgebase.user.controller.dto;

import com.janondra.mdknowledgebase.user.controller.validation.email.ValidEmail;
import com.janondra.mdknowledgebase.user.controller.validation.quarterhour.ValidQuarterHour;
import com.janondra.mdknowledgebase.user.controller.validation.timezone.ValidTimeZone;

import java.time.LocalTime;

public record ModifyUserDTO(
    @ValidEmail String email,
    @ValidTimeZone String timeZone,
    boolean dailyMailEnabled,
    @ValidQuarterHour LocalTime dailyMailTime
) {}
