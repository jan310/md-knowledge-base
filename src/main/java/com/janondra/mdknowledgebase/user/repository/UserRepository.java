package com.janondra.mdknowledgebase.user.repository;

import com.janondra.mdknowledgebase.user.model.UpdateUser;
import com.janondra.mdknowledgebase.user.model.User;
import com.janondra.mdknowledgebase.user.repository.rowmappers.UserRowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepository {

    private static final UserRowMapper userRowMapper = new UserRowMapper();

    private final JdbcClient jdbcClient;

    public UserRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public User createUser(String authId, String email) {
        return jdbcClient
            .sql(
                """
                INSERT INTO users (
                    auth_id,
                    email
                )
                VALUES (
                    :authId,
                    :email
                )
                RETURNING
                    id,
                    email,
                    time_zone,
                    daily_mail_enabled,
                    daily_mail_time,
                    daily_mail_tags;
                """
            )
            .param("authId", authId)
            .param("email", email)
            .query(userRowMapper)
            .single();
    }

    public Optional<User> getUser(String authId) {
        return jdbcClient
            .sql(
                """
                SELECT
                    id,
                    email,
                    time_zone,
                    daily_mail_enabled,
                    daily_mail_time,
                    daily_mail_tags
                FROM users
                WHERE auth_id = :authId;
                """
            )
            .param("authId", authId)
            .query(userRowMapper)
            .optional();
    }

    public void updateUserEmail(String authId, String newEmail) {
        jdbcClient
            .sql(
                """
                UPDATE users
                SET email = :newEmail
                WHERE auth_id = :authId;
                """
            )
            .param("authId", authId)
            .param("newEmail", newEmail)
            .update();
    }

    public UUID getUserIdByAuthId(String authId) {
        return jdbcClient
            .sql("SELECT id FROM users WHERE auth_id = :authId;")
            .param("authId", authId)
            .query(UUID.class)
            .single();
    }

    public void updateUser(UpdateUser updateUser) {
        jdbcClient
            .sql(
                """
                UPDATE users SET
                    time_zone = :timeZone,
                    daily_mail_enabled = :dailyMailEnabled,
                    daily_mail_time = :dailyMailTime,
                    daily_mail_tags = :dailyMailTags
                WHERE auth_id = :authId;
                """
            )
            .param("timeZone", updateUser.timeZone())
            .param("dailyMailEnabled", updateUser.dailyMailEnabled())
            .param("dailyMailTime", updateUser.dailyMailTime())
            .param("dailyMailTags", updateUser.dailyMailTags().toArray(String[]::new))
            .param("authId", updateUser.authId())
            .update();
    }

    public void deleteUser(String authId) {
        jdbcClient
            .sql("DELETE FROM users WHERE auth_id = :authId;")
            .param("authId", authId)
            .update();
    }

}
