package com.janondra.mdknowledgebase.folder.repository.rowmappers;

import com.janondra.mdknowledgebase.folder.model.Folder;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class FolderRowMapper implements RowMapper<Folder> {
    @Override
    public Folder mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Folder(
            rs.getObject("id", UUID.class),
            rs.getObject("owner_id", UUID.class),
            rs.getObject("parent_id", UUID.class),
            rs.getString("folder_name")
        );
    }
}
