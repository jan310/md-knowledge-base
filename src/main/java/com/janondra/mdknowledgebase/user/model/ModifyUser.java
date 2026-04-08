package com.janondra.mdknowledgebase.user.model;

import java.time.LocalTime;
import java.util.List;

public record ModifyUser(
    String authId,
    String timeZone,
    boolean dailyMailEnabled,
    LocalTime dailyMailTime,
    List<String> dailyMailTags
) {}
