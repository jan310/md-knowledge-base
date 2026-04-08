package com.janondra.mdknowledgebase.user.repository;

import com.janondra.mdknowledgebase.user.model.CreateUser;
import com.janondra.mdknowledgebase.user.model.ModifyUser;
import com.janondra.mdknowledgebase.user.model.User;
import com.janondra.mdknowledgebase.user.repository.exceptions.EmailAlreadyInUseException;
import com.janondra.mdknowledgebase.user.repository.rowmappers.UserRowMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class UserRepository {

    private static final String USERS_EMAIL_UNIQUE_VIOLATION = "violates unique constraint \"uq_users_email\"";

    private static final UserRowMapper userRowMapper = new UserRowMapper();

    private final JdbcClient jdbcClient;

    public UserRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public UUID createUser(CreateUser createUser) {
        try {
            return jdbcClient
                .sql(
                    """
                    INSERT INTO users (
                        auth_id,
                        email,
                        time_zone
                    )
                    VALUES (
                        :authId,
                        :email,
                        :timeZone
                    )
                    RETURNING id;
                    """
                )
                .param("authId", createUser.authId())
                .param("email", createUser.email())
                .param("timeZone", createUser.timeZone())
                .query(UUID.class)
                .single();
        } catch (DataIntegrityViolationException e) {
            if (e.getMostSpecificCause().getMessage().contains(USERS_EMAIL_UNIQUE_VIOLATION)) {
                throw new EmailAlreadyInUseException(e);
            }
            throw e;
        }
    }

    public User getUser(String authId) {
        return jdbcClient
            .sql(
                """
                SELECT
                    id,
                    auth_id,
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
            .single();
    }

    public UUID getUserIdByAuthId(String authId) {
        return jdbcClient
            .sql("SELECT id FROM users WHERE auth_id = :authId;")
            .param("authId", authId)
            .query(UUID.class)
            .single();
    }

    public void modifyUser(ModifyUser modifyUser) {
        try {
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
                .param("timeZone", modifyUser.timeZone())
                .param("dailyMailEnabled", modifyUser.dailyMailEnabled())
                .param("dailyMailTime", modifyUser.dailyMailTime())
                .param("dailyMailTags", modifyUser.dailyMailTags().toArray(String[]::new))
                .param("authId", modifyUser.authId())
                .update();
        } catch (DataIntegrityViolationException e) {
            if (e.getMostSpecificCause()
                .getMessage()
                .contains(USERS_EMAIL_UNIQUE_VIOLATION)) {
                throw new EmailAlreadyInUseException(e);
            }
            throw e;
        }
    }

    public void deleteUser(String authId) {
        jdbcClient
            .sql("DELETE FROM users WHERE auth_id = :authId;")
            .param("authId", authId)
            .update();
    }

}
