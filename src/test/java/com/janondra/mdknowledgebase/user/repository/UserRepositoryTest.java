package com.janondra.mdknowledgebase.user.repository;

import com.janondra.mdknowledgebase.helper.DatabaseIntegrationTest;
import com.janondra.mdknowledgebase.user.model.UpdateUser;
import com.janondra.mdknowledgebase.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(UserRepository.class)
class UserRepositoryTest extends DatabaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcClient jdbcClient;

    @Nested
    class CreateUser {

        @Test
        void createsUserWithDefaultSettings() {
            User user = userRepository.createUser("auth-id", "jane@example.com");

            assertThat(user.id()).isNotNull();
            assertThat(user.email()).isEqualTo("jane@example.com");
            assertThat(user.timeZone()).isEqualTo("Europe/Berlin");
            assertThat(user.dailyMailEnabled()).isFalse();
            assertThat(user.dailyMailTime()).isEqualTo(LocalTime.of(6, 0));
            assertThat(user.dailyMailTags()).isEmpty();

            PersistedUser persistedUser = findPersistedUser("auth-id");

            assertThat(persistedUser.id()).isEqualTo(user.id());
            assertThat(persistedUser.authId()).isEqualTo("auth-id");
            assertThat(persistedUser.email()).isEqualTo("jane@example.com");
            assertThat(persistedUser.timeZone()).isEqualTo("Europe/Berlin");
            assertThat(persistedUser.dailyMailEnabled()).isFalse();
            assertThat(persistedUser.dailyMailTime()).isEqualTo(LocalTime.of(6, 0));
            assertThat(persistedUser.dailyMailTags()).isEmpty();
        }

        @Test
        void throwsExceptionWhenAuthIdAlreadyExists() {
            userRepository.createUser("duplicate-auth-id", "first@example.com");

            assertThatThrownBy(
                () -> userRepository.createUser("duplicate-auth-id", "second@example.com")
            )
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void throwsExceptionWhenEmailAlreadyExists() {
            userRepository.createUser("first-email-auth-id", "duplicate@example.com");

            assertThatThrownBy(
                () -> userRepository.createUser("second-email-auth-id", "duplicate@example.com")
            )
                .isInstanceOf(DataIntegrityViolationException.class);
        }

    }

    @Nested
    class GetUser {

        @Test
        void returnsUserWhenAuthIdExists() {
            UUID userId = insertUser(
                "get-user-auth-id",
                "get-user@example.com",
                "UTC",
                true,
                LocalTime.of(7, 45),
                List.of("spring", "testing")
            );

            var user = userRepository.getUser("get-user-auth-id");

            assertThat(user).isPresent();
            assertThat(user.get().id()).isEqualTo(userId);
            assertThat(user.get().email()).isEqualTo("get-user@example.com");
            assertThat(user.get().timeZone()).isEqualTo("UTC");
            assertThat(user.get().dailyMailEnabled()).isTrue();
            assertThat(user.get().dailyMailTime()).isEqualTo(LocalTime.of(7, 45));
            assertThat(user.get().dailyMailTags()).containsExactly("spring", "testing");
        }

        @Test
        void returnsEmptyOptionalWhenAuthIdDoesNotExist() {
            var user = userRepository.getUser("missing-user-auth-id");

            assertThat(user).isEmpty();
        }

    }

    @Nested
    class UpdateUserEmail {

        @Test
        void updatesEmailWhenAuthIdExists() {
            User user = userRepository.createUser("update-email-auth-id", "old-email@example.com");

            userRepository.updateUserEmail("update-email-auth-id", "new-email@example.com");

            PersistedUser persistedUser = findPersistedUser("update-email-auth-id");
            assertThat(persistedUser.id()).isEqualTo(user.id());
            assertThat(persistedUser.email()).isEqualTo("new-email@example.com");
        }

        @Test
        void doesNothingWhenAuthIdDoesNotExist() {
            userRepository.updateUserEmail("missing-update-email-auth-id", "new-email@example.com");

            assertThat(countUsers()).isZero();
        }

    }

    @Nested
    class GetUserIdByAuthId {

        @Test
        void returnsUserIdWhenAuthIdExists() {
            User user = userRepository.createUser("get-user-id-auth-id", "get-user-id@example.com");

            UUID result = userRepository.getUserIdByAuthId("get-user-id-auth-id");

            assertThat(result).isEqualTo(user.id());
        }

        @Test
        void throwsExceptionWhenAuthIdDoesNotExist() {
            assertThatThrownBy(
                () -> userRepository.getUserIdByAuthId("missing-user-id-auth-id")
            )
                .isInstanceOf(EmptyResultDataAccessException.class);
        }

    }

    @Nested
    class UpdateUserSettings {

        @Test
        void updatesUserSettingsWhenAuthIdExists() {
            User user = userRepository.createUser("update-user-auth-id", "update-user@example.com");

            userRepository.updateUser(
                new UpdateUser(
                    "update-user-auth-id",
                    "UTC",
                    true,
                    LocalTime.of(18, 30),
                    List.of("java", "postgres")
                )
            );

            PersistedUser persistedUser = findPersistedUser("update-user-auth-id");
            assertThat(persistedUser.id()).isEqualTo(user.id());
            assertThat(persistedUser.email()).isEqualTo("update-user@example.com");
            assertThat(persistedUser.timeZone()).isEqualTo("UTC");
            assertThat(persistedUser.dailyMailEnabled()).isTrue();
            assertThat(persistedUser.dailyMailTime()).isEqualTo(LocalTime.of(18, 30));
            assertThat(persistedUser.dailyMailTags()).containsExactly("java", "postgres");
        }

        @Test
        void doesNothingWhenAuthIdDoesNotExist() {
            userRepository.updateUser(
                new UpdateUser(
                    "missing-update-user-auth-id",
                    "UTC",
                    true,
                    LocalTime.of(18, 30),
                    List.of("java", "postgres")
                )
            );

            assertThat(countUsers()).isZero();
        }

    }

    @Nested
    class DeleteUser {

        @Test
        void deletesUserWhenAuthIdExists() {
            userRepository.createUser("delete-user-auth-id", "delete-user@example.com");

            userRepository.deleteUser("delete-user-auth-id");

            assertThat(countUsers()).isZero();
        }

        @Test
        void doesNothingWhenAuthIdDoesNotExist() {
            userRepository.createUser("existing-delete-user-auth-id", "existing-delete-user@example.com");

            userRepository.deleteUser("missing-delete-user-auth-id");

            assertThat(countUsers()).isEqualTo(1);
            assertThat(findPersistedUser("existing-delete-user-auth-id").email())
                .isEqualTo("existing-delete-user@example.com");
        }

    }

    private UUID insertUser(
        String authId,
        String email,
        String timeZone,
        boolean dailyMailEnabled,
        LocalTime dailyMailTime,
        List<String> dailyMailTags
    ) {
        return jdbcClient
            .sql(
                """
                INSERT INTO users (
                    auth_id,
                    email,
                    time_zone,
                    daily_mail_enabled,
                    daily_mail_time,
                    daily_mail_tags
                )
                VALUES (
                    :authId,
                    :email,
                    :timeZone,
                    :dailyMailEnabled,
                    :dailyMailTime,
                    :dailyMailTags
                )
                RETURNING id;
                """
            )
            .param("authId", authId)
            .param("email", email)
            .param("timeZone", timeZone)
            .param("dailyMailEnabled", dailyMailEnabled)
            .param("dailyMailTime", dailyMailTime)
            .param("dailyMailTags", dailyMailTags.toArray(String[]::new))
            .query(UUID.class)
            .single();
    }

    private long countUsers() {
        return jdbcClient
            .sql("SELECT COUNT(*) FROM users;")
            .query(Long.class)
            .single();
    }

    private PersistedUser findPersistedUser(String authId) {
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
            .query((rs, rowNum) -> new PersistedUser(
                rs.getObject("id", UUID.class),
                rs.getString("auth_id"),
                rs.getString("email"),
                rs.getString("time_zone"),
                rs.getBoolean("daily_mail_enabled"),
                rs.getTime("daily_mail_time").toLocalTime(),
                toStringList(rs.getArray("daily_mail_tags"))
            ))
            .single();
    }

    private static List<String> toStringList(java.sql.Array sqlArray) throws SQLException {
        try {
            return List.of((String[]) sqlArray.getArray());
        } finally {
            sqlArray.free();
        }
    }

    private record PersistedUser(
        UUID id,
        String authId,
        String email,
        String timeZone,
        boolean dailyMailEnabled,
        LocalTime dailyMailTime,
        List<String> dailyMailTags
    ) {
    }

}
