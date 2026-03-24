package com.janondra.mdknowledgebase.user.model;

import java.time.LocalTime;

public record ModifyUser(
    String authId,
    String email,
    String timeZone,
    boolean dailyMailEnabled,
    LocalTime dailyMailTime
) {}
