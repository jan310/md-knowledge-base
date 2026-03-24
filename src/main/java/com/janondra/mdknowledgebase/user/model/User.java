package com.janondra.mdknowledgebase.user.model;

import java.time.LocalTime;
import java.util.UUID;

public record User(
    UUID id,
    String authId,
    String email,
    String timeZone,
    boolean dailyMailEnabled,
    LocalTime dailyMailTime
) {}
