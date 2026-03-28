package com.janondra.mdknowledgebase.document.repository.rowmappers;

import com.janondra.mdknowledgebase.document.model.DailyMailTarget;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DailyMailTargetRowMapper implements RowMapper<DailyMailTarget> {
    @Override
    public DailyMailTarget mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new DailyMailTarget(
            rs.getString("email"),
            rs.getString("file_name"),
            rs.getString("content"),
            toStringList(rs.getArray("questions"))
        );
    }

    private static List<String> toStringList(Array sqlArray) throws SQLException {
        try {
            return List.of((String[]) sqlArray.getArray());
        } finally {
            sqlArray.free();
        }
    }
}
