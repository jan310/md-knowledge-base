package com.janondra.mdknowledgebase.document.repository;

import com.janondra.mdknowledgebase.document.model.CreateDocument;
import com.janondra.mdknowledgebase.document.model.DailyMailTarget;
import com.janondra.mdknowledgebase.document.model.Document;
import com.janondra.mdknowledgebase.document.model.DocumentRef;
import com.janondra.mdknowledgebase.document.repository.rowmappers.DailyMailTargetRowMapper;
import com.janondra.mdknowledgebase.document.repository.rowmappers.DocumentRefRowMapper;
import com.janondra.mdknowledgebase.document.repository.rowmappers.DocumentRowMapper;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class DocumentRepository {

    private static final DocumentRowMapper documentRowMapper = new DocumentRowMapper();
    private static final DocumentRefRowMapper documentRefRowMapper = new DocumentRefRowMapper();
    private static final DailyMailTargetRowMapper dailyMailTargetRowMapper = new DailyMailTargetRowMapper();

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
                    content
                )
                VALUES (
                    :ownerId,
                    :fileName,
                    :tags,
                    :content
                )
                RETURNING id;
                """
            )
            .param("ownerId", createDocument.ownerId())
            .param("fileName", createDocument.fileName())
            .param("tags", createDocument.tags().toArray(String[]::new))
            .param("content", createDocument.content())
            .query(UUID.class)
            .single();
    }

    public Document getDocumentByIdAndOwnerId(UUID id, UUID ownerId) {
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
                      AND tags @> :tags::text[]
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
                      AND tags @> :tags::text[]
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
                WITH q AS (
                    SELECT websearch_to_tsquery('simple', :searchText) AS query
                )
                SELECT
                    id,
                    file_name
                FROM documents, q
                WHERE owner_id = :ownerId
                  AND content_search_vector @@ q.query
                ORDER BY
                    ts_rank(content_search_vector, q.query) DESC,
                    file_name
                LIMIT :limit
                OFFSET :offset;
                """
            )
            .param("ownerId", ownerId)
            .param("searchText", searchText)
            .param("limit", pageSize)
            .param("offset", offset)
            .query(documentRefRowMapper)
            .list();
    }

    public void updateDocumentFilename(UUID id, UUID ownerId, String newFileName) {
        jdbcClient
            .sql(
                """
                UPDATE documents
                SET file_name = :fileName
                WHERE id = :id AND owner_id = :ownerId;
                """
            )
            .param("id", id)
            .param("ownerId", ownerId)
            .param("fileName", newFileName)
            .update();
    }

    public void updateDocumentTags(UUID id, UUID ownerId, List<String> newTags) {
        jdbcClient
            .sql(
                """
                UPDATE documents
                SET tags = :newTags
                WHERE id = :id AND owner_id = :ownerId;
                """
            )
            .param("id", id)
            .param("ownerId", ownerId)
            .param("newTags", newTags.toArray(String[]::new))
            .update();
    }

    public void updateDocumentContent(UUID id, UUID ownerId, String newContent) {
        jdbcClient
            .sql(
                """
                UPDATE documents
                SET
                    content = :newContent,
                    questions = '{}'
                WHERE id = :id AND owner_id = :ownerId;
                """
            )
            .param("id", id)
            .param("ownerId", ownerId)
            .param("newContent", newContent)
            .update();
    }

    public void updateDocumentQuestions(UUID id, List<String> newQuestions) {
        jdbcClient
            .sql(
                """
                UPDATE documents
                SET questions = :newQuestions
                WHERE id = :id;
                """
            )
            .param("id", id)
            .param("newQuestions", newQuestions.toArray(String[]::new))
            .update();
    }

    public void deleteDocument(UUID id, UUID ownerId) {
        jdbcClient
            .sql("DELETE FROM documents WHERE id = :id AND owner_id = :ownerId;")
            .param("id", id)
            .param("ownerId", ownerId)
            .update();
    }

    public List<DailyMailTarget> getDailyMailTargets(OffsetDateTime utcDateTime) {
        return jdbcClient
            .sql(
                """
                    SELECT
                        u.email,
                        d_random.file_name,
                        d_random.content,
                        d_random.questions
                    FROM users u
                    INNER JOIN LATERAL (
                        SELECT count(*) as total
                        FROM documents
                        WHERE owner_id = u.id
                          AND questions <> '{}'
                    ) cnt ON true
                    INNER JOIN LATERAL (
                        SELECT d.file_name, d.content, d.questions
                        FROM documents d
                        WHERE d.owner_id = u.id
                          AND questions <> '{}'
                        OFFSET floor(random() * GREATEST(cnt.total, 1))
                        LIMIT 1
                    ) d_random ON true
                    WHERE
                        u.daily_mail_enabled = true
                        AND u.daily_mail_time = (:utcDateTime AT TIME ZONE u.time_zone)::time;
                """
            )
            .param("utcDateTime", utcDateTime)
            .query(dailyMailTargetRowMapper)
            .list();
    }

}
