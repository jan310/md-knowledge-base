package com.janondra.mdknowledgebase.user.controller.dto;

import java.time.LocalTime;
import java.util.List;

public record UserResponseDTO(
    String email,
    String timeZone,
    boolean dailyMailEnabled,
    LocalTime dailyMailTime,
    List<String> dailyMailTags
) {}
