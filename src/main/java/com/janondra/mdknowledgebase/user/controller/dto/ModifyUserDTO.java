package com.janondra.mdknowledgebase.user.controller.dto;

import com.janondra.mdknowledgebase.user.controller.validation.quarterhour.ValidQuarterHour;
import com.janondra.mdknowledgebase.user.controller.validation.timezone.ValidTimeZone;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;
import java.util.List;

public record ModifyUserDTO(
    @ValidTimeZone String timeZone,
    boolean dailyMailEnabled,
    @ValidQuarterHour LocalTime dailyMailTime,
    @Size(max = 5) List<@NotBlank String> dailyMailTags
) {}
