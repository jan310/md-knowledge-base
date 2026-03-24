package com.janondra.mdknowledgebase.user.controller.dto;

import java.time.LocalTime;

public record UserResponseDTO(
    String email,
    String timeZone,
    boolean dailyMailEnabled,
    LocalTime dailyMailTime
) {}
