package com.janondra.mdknowledgebase.document.repository.rowmappers;

import com.janondra.mdknowledgebase.document.model.DocumentContent;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class DocumentContentRowMapper implements RowMapper<DocumentContent> {
    @Override
    public DocumentContent mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new DocumentContent(
            rs.getObject("id", UUID.class),
            rs.getString("content")
        );
    }
}
