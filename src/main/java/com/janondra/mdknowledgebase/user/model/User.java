package com.janondra.mdknowledgebase.user.model;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record User(
    UUID id,
    String email,
    String timeZone,
    boolean dailyMailEnabled,
    LocalTime dailyMailTime,
    List<String> dailyMailTags
) {

    public User withEmail(String email) {
        return new User(
            id,
            email,
            timeZone,
            dailyMailEnabled,
            dailyMailTime,
            dailyMailTags
        );
    }
}
