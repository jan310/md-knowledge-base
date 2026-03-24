package com.janondra.mdknowledgebase.document.repository;

import com.janondra.mdknowledgebase.document.model.DocumentRef;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class DocumentRefRowMapper implements RowMapper<DocumentRef> {
    @Override
    public DocumentRef mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new DocumentRef(
            rs.getObject("id", UUID.class),
            rs.getString("file_name")
        );
    }
}
