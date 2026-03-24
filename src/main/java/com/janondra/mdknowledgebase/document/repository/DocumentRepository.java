package com.janondra.mdknowledgebase.document.repository;

import com.janondra.mdknowledgebase.document.model.CreateDocument;
import com.janondra.mdknowledgebase.document.model.Document;
import com.janondra.mdknowledgebase.document.model.DocumentRef;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class DocumentRepository {

    private static final DocumentRowMapper documentRowMapper = new DocumentRowMapper();
    private static final DocumentRefRowMapper documentRefRowMapper = new DocumentRefRowMapper();

    private final JdbcClient jdbcClient;

    public DocumentRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public UUID saveDocument(CreateDocument createDocument) {
        return jdbcClient
            .sql(
                """
                INSERT INTO documents (
                    owner_id,
                    file_name,
                    tags,
                    content,
                    questions
                )
                VALUES (
                    :ownerId,
                    :fileName,
                    :tags,
                    :content,
                    :questions
                )
                RETURNING id;
                """
            )
            .param("ownerId", createDocument.ownerId())
            .param("fileName", createDocument.fileName())
            .param("tags", createDocument.tags().toArray(String[]::new))
            .param("content", createDocument.content())
            .param("questions", createDocument.questions().toArray(String[]::new))
            .query(UUID.class)
            .single();
    }

    public Document getDocumentById(UUID id, UUID ownerId) {
        return jdbcClient
            .sql(
                """
                SELECT
                    id,
                    owner_id,
                    file_name,
                    tags,
                    content,
                    questions
                FROM documents
                WHERE id = :id
                  AND owner_id = :ownerId;
                """
            )
            .param("id", id)
            .param("ownerId", ownerId)
            .query(documentRowMapper)
            .single();
    }

    public List<DocumentRef> getDocumentRefsByOwnerId(UUID ownerId, int pageSize, @Nullable String lastFileName) {
        if (lastFileName == null) {
            return jdbcClient
                .sql(
                    """
                    SELECT id, file_name
                    FROM documents
                    WHERE owner_id = :ownerId
                    ORDER BY file_name
                    LIMIT :limit
                    """
                )
                .param("ownerId", ownerId)
                .param("limit", pageSize)
                .query(documentRefRowMapper)
                .list();
        } else {
            return jdbcClient
                .sql(
                    """
                    SELECT id, file_name
                    FROM documents
                    WHERE owner_id = :ownerId
                      AND file_name > :lastFileName
                    ORDER BY file_name
                    LIMIT :limit
                    """
                )
                .param("ownerId", ownerId)
                .param("lastFileName", lastFileName)
                .param("limit", pageSize)
                .query(documentRefRowMapper)
                .list();
        }
    }

    public List<DocumentRef> getDocumentRefsByOwnerIdAndTags(
        UUID ownerId,
        List<String> tags,
        int pageSize,
        @Nullable String lastFileName
    ) {
        if (lastFileName == null) {
            return jdbcClient
                .sql(
                    """
                    SELECT id, file_name
                    FROM documents
                    WHERE owner_id = :ownerId
                      AND tags @> :tags
                    ORDER BY file_name
                    LIMIT :limit
                    """
                )
                .param("ownerId", ownerId)
                .param("tags", tags.toArray(String[]::new))
                .param("limit", pageSize)
                .query(documentRefRowMapper)
                .list();
        } else {
            return jdbcClient
                .sql(
                    """
                    SELECT id, file_name
                    FROM documents
                    WHERE owner_id = :ownerId
                      AND tags @> :tags
                      AND file_name > :lastFileName
                    ORDER BY file_name
                    LIMIT :limit
                    """
                )
                .param("ownerId", ownerId)
                .param("tags", tags.toArray(String[]::new))
                .param("lastFileName", lastFileName)
                .param("limit", pageSize)
                .query(documentRefRowMapper)
                .list();
        }
    }

    public List<DocumentRef> getDocumentRefsByOwnerIdAndSearchText(
        UUID ownerId,
        String searchText,
        int pageSize,
        int offset
    ) {
        return jdbcClient
            .sql(
                """
                SELECT id, file_name
                FROM documents
                WHERE owner_id = :ownerId
                  AND content_search_vector @@ websearch_to_tsquery('simple', :searchText)
                ORDER BY ts_rank(content_search_vector, websearch_to_tsquery('simple', :searchText)) DESC, file_name
                LIMIT :limit
                OFFSET :offset
                """
            )
            .param("ownerId", ownerId)
            .param("searchText", searchText)
            .param("limit", pageSize)
            .param("offset", offset)
            .query(documentRefRowMapper)
            .list();
    }

}
