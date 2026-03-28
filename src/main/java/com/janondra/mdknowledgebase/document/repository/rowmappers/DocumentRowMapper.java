package com.janondra.mdknowledgebase.document.repository.rowmappers;

import com.janondra.mdknowledgebase.document.model.Document;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class DocumentRowMapper implements RowMapper<Document> {
    @Override
    public Document mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Document(
            rs.getObject("id", UUID.class),
            rs.getObject("owner_id", UUID.class),
            rs.getString("file_name"),
            toStringList(rs.getArray("tags")),
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
