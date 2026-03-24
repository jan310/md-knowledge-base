package com.janondra.mdknowledgebase.user.repository;

import com.janondra.mdknowledgebase.user.model.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UserRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new User(
            rs.getObject("id", UUID.class),
            rs.getString("auth_id"),
            rs.getString("email"),
            rs.getString("time_zone"),
            rs.getBoolean("daily_mail_enabled"),
            rs.getTime("daily_mail_time").toLocalTime()
        );
    }
}
