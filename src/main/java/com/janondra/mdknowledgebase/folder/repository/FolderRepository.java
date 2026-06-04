package com.janondra.mdknowledgebase.folder.repository;

import com.janondra.mdknowledgebase.folder.model.Folder;
import com.janondra.mdknowledgebase.folder.repository.rowmappers.FolderRowMapper;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class FolderRepository {

    private static final FolderRowMapper folderRowMapper = new FolderRowMapper();

    private final JdbcClient jdbcClient;

    public FolderRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public UUID createFolder(UUID ownerId, @Nullable UUID parentId, String folderName) {
        return jdbcClient
            .sql(
                """
                INSERT INTO folders (
                    owner_id,
                    parent_id,
                    folder_name
                )
                VALUES (
                    :ownerId,
                    :parentId,
                    :folderName
                )
                RETURNING id;
                """
            )
            .param("ownerId", ownerId)
            .param("parentId", parentId)
            .param("folderName", folderName)
            .query(UUID.class)
            .single();
    }

    public List<Folder> getSubFolders(UUID ownerId, @Nullable UUID parentId) {
        return jdbcClient
            .sql(
                """
                SELECT
                    id,
                    owner_id,
                    parent_id,
                    folder_name
                FROM folders
                WHERE owner_id = :ownerId
                  AND parent_id IS NOT DISTINCT FROM :parentId
                ORDER BY folder_name;
                """
            )
            .param("ownerId", ownerId)
            .param("parentId", parentId)
            .query(folderRowMapper)
            .list();
    }

    public void renameFolder(UUID id, UUID ownerId, String newFolderName) {
        jdbcClient
            .sql(
                """
                UPDATE folders
                SET folder_name = :folderName
                WHERE id = :id
                  AND owner_id = :ownerId;
                """
            )
            .param("id", id)
            .param("ownerId", ownerId)
            .param("folderName", newFolderName)
            .update();
    }

    public boolean moveFolder(UUID id, UUID ownerId, @Nullable UUID newParentId) {
        if (newParentId == null) {
            int updatedRows = jdbcClient.sql(
                    """
                    UPDATE folders
                    SET parent_id = NULL
                    WHERE id = :id
                      AND owner_id = :ownerId;
                    """
                )
                .param("id", id)
                .param("ownerId", ownerId)
                .update();

            return updatedRows == 1;
        }

        int updatedRows = jdbcClient.sql(
                """
                WITH RECURSIVE parent_chain AS (
                    SELECT id, parent_id
                    FROM folders
                    WHERE id = :newParentId AND owner_id = :ownerId
                
                    UNION ALL
                
                    SELECT f.id, f.parent_id
                    FROM folders f
                    JOIN parent_chain pc ON f.id = pc.parent_id
                    WHERE f.owner_id = :ownerId
                )
                UPDATE folders
                SET parent_id = :newParentId
                WHERE id = :id
                  AND owner_id = :ownerId
                  AND EXISTS (
                      SELECT 1
                      FROM parent_chain
                  )
                  AND NOT EXISTS (
                      SELECT 1
                      FROM parent_chain
                      WHERE id = :id
                  );
                """
            )
            .param("id", id)
            .param("ownerId", ownerId)
            .param("newParentId", newParentId)
            .update();

        return updatedRows == 1;
    }

    public void deleteFolder(UUID id, UUID ownerId) {
        jdbcClient
            .sql(
                """
                DELETE FROM folders
                WHERE id = :id
                  AND owner_id = :ownerId;
                """
            )
            .param("id", id)
            .param("ownerId", ownerId)
            .update();
    }

}
