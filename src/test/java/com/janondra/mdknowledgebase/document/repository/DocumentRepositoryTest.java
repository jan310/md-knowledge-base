package com.janondra.mdknowledgebase.document.repository;

import com.janondra.mdknowledgebase.document.model.CreateDocument;
import com.janondra.mdknowledgebase.document.model.DailyMailTarget;
import com.janondra.mdknowledgebase.document.model.Document;
import com.janondra.mdknowledgebase.document.model.DocumentRef;
import com.janondra.mdknowledgebase.helper.DatabaseIntegrationTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.sql.SQLException;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(DocumentRepository.class)
class DocumentRepositoryTest extends DatabaseIntegrationTest {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private JdbcClient jdbcClient;

    @Nested
    class SaveDocument {

        @Test
        void savesDocument() {
            UUID ownerId = insertUser("save-document-owner-id", "save-document-owner@example.com");
            CreateDocument createDocument = new CreateDocument(
                ownerId,
                "notes.md",
                List.of("java", "spring"),
                "Spring JDBC notes"
            );

            UUID documentId = documentRepository.saveDocument(createDocument);

            PersistedDocument persistedDocument = findPersistedDocument(documentId);
            assertThat(persistedDocument.id()).isEqualTo(documentId);
            assertThat(persistedDocument.ownerId()).isEqualTo(ownerId);
            assertThat(persistedDocument.fileName()).isEqualTo("notes.md");
            assertThat(persistedDocument.tags()).containsExactly("java", "spring");
            assertThat(persistedDocument.content()).isEqualTo("Spring JDBC notes");
            assertThat(persistedDocument.questions()).isEmpty();
        }

        @Test
        void throwsExceptionWhenOwnerIdDoesNotExist() {
            CreateDocument createDocument = new CreateDocument(
                UUID.randomUUID(),
                "orphan.md",
                List.of("java"),
                "Orphan document"
            );

            assertThatThrownBy(
                () -> documentRepository.saveDocument(createDocument)
            )
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void throwsExceptionWhenFileNameAlreadyExistsForOwner() {
            UUID ownerId = insertUser("duplicate-document-owner-id", "duplicate-document-owner@example.com");
            documentRepository.saveDocument(
                new CreateDocument(ownerId, "duplicate.md", List.of("java"), "First document")
            );

            assertThatThrownBy(
                () -> documentRepository.saveDocument(
                    new CreateDocument(ownerId, "duplicate.md", List.of("spring"), "Second document")
                )
            )
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void allowsSameFileNameForDifferentOwners() {
            UUID firstOwnerId = insertUser("first-same-file-owner-id", "first-same-file-owner@example.com");
            UUID secondOwnerId = insertUser("second-same-file-owner-id", "second-same-file-owner@example.com");

            UUID firstDocumentId = documentRepository.saveDocument(
                new CreateDocument(firstOwnerId, "shared.md", List.of("java"), "First owner document")
            );
            UUID secondDocumentId = documentRepository.saveDocument(
                new CreateDocument(secondOwnerId, "shared.md", List.of("spring"), "Second owner document")
            );

            assertThat(findPersistedDocument(firstDocumentId).fileName()).isEqualTo("shared.md");
            assertThat(findPersistedDocument(secondDocumentId).fileName()).isEqualTo("shared.md");
        }

    }

    @Nested
    class GetDocumentByIdAndOwnerId {

        @Test
        void returnsDocumentWhenIdAndOwnerIdMatch() {
            UUID ownerId = insertUser("get-document-owner-id", "get-document-owner@example.com");
            UUID documentId = documentRepository.saveDocument(
                new CreateDocument(
                    ownerId,
                    "document.md",
                    List.of("java", "spring"),
                    "Document content"
                )
            );

            Document document = documentRepository.getDocumentByIdAndOwnerId(documentId, ownerId);

            assertThat(document.id()).isEqualTo(documentId);
            assertThat(document.ownerId()).isEqualTo(ownerId);
            assertThat(document.fileName()).isEqualTo("document.md");
            assertThat(document.tags()).containsExactly("java", "spring");
            assertThat(document.content()).isEqualTo("Document content");
            assertThat(document.questions()).isEmpty();
        }

        @Test
        void throwsExceptionWhenDocumentDoesNotExist() {
            UUID ownerId = insertUser("missing-document-owner-id", "missing-document-owner@example.com");

            assertThatThrownBy(
                () -> documentRepository.getDocumentByIdAndOwnerId(UUID.randomUUID(), ownerId)
            )
                .isInstanceOf(EmptyResultDataAccessException.class);
        }

        @Test
        void throwsExceptionWhenOwnerIdDoesNotMatch() {
            UUID ownerId = insertUser("document-owner-id", "document-owner@example.com");
            UUID otherOwnerId = insertUser("other-document-owner-id", "other-document-owner@example.com");
            UUID documentId = documentRepository.saveDocument(
                new CreateDocument(
                    ownerId,
                    "owner-document.md",
                    List.of("java"),
                    "Owner document content"
                )
            );

            assertThatThrownBy(
                () -> documentRepository.getDocumentByIdAndOwnerId(documentId, otherOwnerId)
            )
                .isInstanceOf(EmptyResultDataAccessException.class);
        }

    }

    @Nested
    class GetDocumentRefsByOwnerId {

        @Test
        void returnsDocumentRefsOrderedByFileNameAndLimitedToPageSize() {
            UUID ownerId = insertUser("document-refs-owner-id", "document-refs-owner@example.com");
            UUID otherOwnerId = insertUser("other-document-refs-owner-id", "other-document-refs-owner@example.com");
            documentRepository.saveDocument(
                new CreateDocument(ownerId, "charlie.md", List.of("java"), "Charlie content")
            );
            UUID alphaDocumentId = documentRepository.saveDocument(
                new CreateDocument(ownerId, "alpha.md", List.of("spring"), "Alpha content")
            );
            UUID bravoDocumentId = documentRepository.saveDocument(
                new CreateDocument(ownerId, "bravo.md", List.of("jdbc"), "Bravo content")
            );
            documentRepository.saveDocument(
                new CreateDocument(otherOwnerId, "aardvark.md", List.of("java"), "Other owner content")
            );

            List<DocumentRef> documentRefs = documentRepository.getDocumentRefsByOwnerId(ownerId, 2, null);

            assertThat(documentRefs)
                .containsExactly(
                    new DocumentRef(alphaDocumentId, "alpha.md"),
                    new DocumentRef(bravoDocumentId, "bravo.md")
                );
        }

        @Test
        void returnsDocumentRefsAfterLastFileName() {
            UUID ownerId = insertUser("document-refs-page-owner-id", "document-refs-page-owner@example.com");
            documentRepository.saveDocument(
                new CreateDocument(ownerId, "alpha.md", List.of("spring"), "Alpha content")
            );
            UUID bravoDocumentId = documentRepository.saveDocument(
                new CreateDocument(ownerId, "bravo.md", List.of("jdbc"), "Bravo content")
            );
            UUID charlieDocumentId = documentRepository.saveDocument(
                new CreateDocument(ownerId, "charlie.md", List.of("java"), "Charlie content")
            );

            List<DocumentRef> documentRefs = documentRepository.getDocumentRefsByOwnerId(ownerId, 10, "alpha.md");

            assertThat(documentRefs)
                .containsExactly(
                    new DocumentRef(bravoDocumentId, "bravo.md"),
                    new DocumentRef(charlieDocumentId, "charlie.md")
                );
        }

        @Test
        void returnsEmptyListWhenOwnerHasNoDocuments() {
            UUID ownerId = insertUser("no-document-refs-owner-id", "no-document-refs-owner@example.com");

            List<DocumentRef> documentRefs = documentRepository.getDocumentRefsByOwnerId(ownerId, 10, null);

            assertThat(documentRefs).isEmpty();
        }

    }

    @Nested
    class GetDocumentRefsByOwnerIdAndTags {

        @Test
        void returnsDocumentRefsMatchingAllTagsOrderedByFileNameAndLimitedToPageSize() {
            UUID ownerId = insertUser("tagged-document-refs-owner-id", "tagged-document-refs-owner@example.com");
            UUID otherOwnerId = insertUser("other-tagged-document-refs-owner-id", "other-tagged-document-refs-owner@example.com");
            UUID alphaDocumentId = documentRepository.saveDocument(
                new CreateDocument(ownerId, "alpha.md", List.of("java", "spring"), "Alpha content")
            );
            UUID bravoDocumentId = documentRepository.saveDocument(
                new CreateDocument(ownerId, "bravo.md", List.of("java", "spring", "jdbc"), "Bravo content")
            );
            documentRepository.saveDocument(
                new CreateDocument(ownerId, "charlie.md", List.of("java", "postgres"), "Charlie content")
            );
            documentRepository.saveDocument(
                new CreateDocument(otherOwnerId, "aardvark.md", List.of("java", "spring"), "Other owner content")
            );

            List<DocumentRef> documentRefs = documentRepository.getDocumentRefsByOwnerIdAndTags(
                ownerId,
                List.of("java", "spring"),
                5,
                null
            );

            assertThat(documentRefs)
                .containsExactly(
                    new DocumentRef(alphaDocumentId, "alpha.md"),
                    new DocumentRef(bravoDocumentId, "bravo.md")
                );
        }

        @Test
        void returnsDocumentRefsMatchingAllTagsAfterLastFileName() {
            UUID ownerId = insertUser("tagged-document-refs-page-owner-id", "tagged-document-refs-page-owner@example.com");
            documentRepository.saveDocument(
                new CreateDocument(ownerId, "alpha.md", List.of("java", "spring"), "Alpha content")
            );
            UUID bravoDocumentId = documentRepository.saveDocument(
                new CreateDocument(ownerId, "bravo.md", List.of("java", "spring"), "Bravo content")
            );
            UUID charlieDocumentId = documentRepository.saveDocument(
                new CreateDocument(ownerId, "charlie.md", List.of("java", "spring", "jdbc"), "Charlie content")
            );
            documentRepository.saveDocument(
                new CreateDocument(ownerId, "delta.md", List.of("java"), "Delta content")
            );

            List<DocumentRef> documentRefs = documentRepository.getDocumentRefsByOwnerIdAndTags(
                ownerId,
                List.of("java", "spring"),
                10,
                "alpha.md"
            );

            assertThat(documentRefs)
                .containsExactly(
                    new DocumentRef(bravoDocumentId, "bravo.md"),
                    new DocumentRef(charlieDocumentId, "charlie.md")
                );
        }

        @Test
        void returnsEmptyListWhenNoDocumentsMatchTags() {
            UUID ownerId = insertUser("no-tagged-document-refs-owner-id", "no-tagged-document-refs-owner@example.com");
            documentRepository.saveDocument(
                new CreateDocument(ownerId, "alpha.md", List.of("java"), "Alpha content")
            );

            List<DocumentRef> documentRefs = documentRepository.getDocumentRefsByOwnerIdAndTags(
                ownerId,
                List.of("spring"),
                10,
                null
            );

            assertThat(documentRefs).isEmpty();
        }

    }

    @Nested
    class GetDocumentRefsByOwnerIdAndSearchText {

        @Test
        void returnsDocumentRefsMatchingSearchTextForOwner() {
            UUID ownerId = insertUser("search-document-refs-owner-id", "search-document-refs-owner@example.com");
            UUID otherOwnerId = insertUser("other-search-document-refs-owner-id", "other-search-document-refs-owner@example.com");
            UUID springDocumentId = documentRepository.saveDocument(
                new CreateDocument(ownerId, "spring.md", List.of("java"), "Spring JDBC repository notes")
            );
            documentRepository.saveDocument(
                new CreateDocument(ownerId, "postgres.md", List.of("database"), "Postgres indexing notes")
            );
            documentRepository.saveDocument(
                new CreateDocument(otherOwnerId, "other-spring.md", List.of("java"), "Spring JDBC notes for another owner")
            );

            List<DocumentRef> documentRefs = documentRepository.getDocumentRefsByOwnerIdAndSearchText(
                ownerId,
                "spring jdbc",
                10,
                0
            );

            assertThat(documentRefs)
                .containsExactly(new DocumentRef(springDocumentId, "spring.md"));
        }

        @Test
        void appliesLimitAndOffsetToSearchResults() {
            UUID ownerId = insertUser("search-page-document-refs-owner-id", "search-page-document-refs-owner@example.com");
            documentRepository.saveDocument(
                new CreateDocument(ownerId, "alpha.md", List.of("java"), "unique pagination search term")
            );
            UUID bravoDocumentId = documentRepository.saveDocument(
                new CreateDocument(ownerId, "bravo.md", List.of("java"), "unique pagination search term")
            );
            UUID charlieDocumentId = documentRepository.saveDocument(
                new CreateDocument(ownerId, "charlie.md", List.of("java"), "unique pagination search term")
            );

            List<DocumentRef> documentRefs = documentRepository.getDocumentRefsByOwnerIdAndSearchText(
                ownerId,
                "unique pagination search term",
                2,
                1
            );

            assertThat(documentRefs)
                .containsExactly(
                    new DocumentRef(bravoDocumentId, "bravo.md"),
                    new DocumentRef(charlieDocumentId, "charlie.md")
                );
        }

        @Test
        void returnsEmptyListWhenSearchTextDoesNotMatch() {
            UUID ownerId = insertUser("no-search-document-refs-owner-id", "no-search-document-refs-owner@example.com");
            documentRepository.saveDocument(
                new CreateDocument(ownerId, "alpha.md", List.of("java"), "Spring JDBC repository notes")
            );

            List<DocumentRef> documentRefs = documentRepository.getDocumentRefsByOwnerIdAndSearchText(
                ownerId,
                "kotlin coroutine",
                10,
                0
            );

            assertThat(documentRefs).isEmpty();
        }

    }

    @Nested
    class UpdateDocumentFilename {

        @Test
        void updatesFilenameWhenDocumentIdAndOwnerIdMatch() {
            UUID ownerId = insertUser("update-filename-owner-id", "update-filename-owner@example.com");
            UUID documentId = documentRepository.saveDocument(
                new CreateDocument(ownerId, "old-name.md", List.of("java"), "Document content")
            );

            documentRepository.updateDocumentFilename(documentId, ownerId, "new-name.md");

            PersistedDocument persistedDocument = findPersistedDocument(documentId);
            assertThat(persistedDocument.fileName()).isEqualTo("new-name.md");
            assertThat(persistedDocument.tags()).containsExactly("java");
            assertThat(persistedDocument.content()).isEqualTo("Document content");
        }

        @Test
        void doesNothingWhenOwnerIdDoesNotMatch() {
            UUID ownerId = insertUser("filename-owner-id", "filename-owner@example.com");
            UUID otherOwnerId = insertUser("other-filename-owner-id", "other-filename-owner@example.com");
            UUID documentId = documentRepository.saveDocument(
                new CreateDocument(ownerId, "unchanged-name.md", List.of("java"), "Document content")
            );

            documentRepository.updateDocumentFilename(documentId, otherOwnerId, "new-name.md");

            assertThat(findPersistedDocument(documentId).fileName()).isEqualTo("unchanged-name.md");
        }

        @Test
        void doesNothingWhenDocumentIdDoesNotExist() {
            UUID ownerId = insertUser("missing-filename-owner-id", "missing-filename-owner@example.com");
            UUID documentId = documentRepository.saveDocument(
                new CreateDocument(ownerId, "unchanged-name.md", List.of("java"), "Document content")
            );

            documentRepository.updateDocumentFilename(UUID.randomUUID(), ownerId, "new-name.md");

            assertThat(findPersistedDocument(documentId).fileName()).isEqualTo("unchanged-name.md");
        }

        @Test
        void throwsExceptionWhenNewFilenameAlreadyExistsForOwner() {
            UUID ownerId = insertUser("duplicate-filename-owner-id", "duplicate-filename-owner@example.com");
            UUID documentId = documentRepository.saveDocument(
                new CreateDocument(ownerId, "old-name.md", List.of("java"), "Document content")
            );
            documentRepository.saveDocument(
                new CreateDocument(ownerId, "existing-name.md", List.of("spring"), "Other document content")
            );

            assertThatThrownBy(
                () -> documentRepository.updateDocumentFilename(documentId, ownerId, "existing-name.md")
            )
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void allowsFilenameAlreadyUsedByDifferentOwner() {
            UUID ownerId = insertUser("shared-filename-owner-id", "shared-filename-owner@example.com");
            UUID otherOwnerId = insertUser("other-shared-filename-owner-id", "other-shared-filename-owner@example.com");
            UUID documentId = documentRepository.saveDocument(
                new CreateDocument(ownerId, "old-name.md", List.of("java"), "Document content")
            );
            documentRepository.saveDocument(
                new CreateDocument(otherOwnerId, "shared-name.md", List.of("spring"), "Other owner content")
            );

            documentRepository.updateDocumentFilename(documentId, ownerId, "shared-name.md");

            assertThat(findPersistedDocument(documentId).fileName()).isEqualTo("shared-name.md");
        }

    }

    @Nested
    class UpdateDocumentTags {

        @Test
        void updatesTagsWhenDocumentIdAndOwnerIdMatch() {
            UUID ownerId = insertUser("update-tags-owner-id", "update-tags-owner@example.com");
            UUID documentId = documentRepository.saveDocument(
                new CreateDocument(ownerId, "tags.md", List.of("old", "tags"), "Document content")
            );

            documentRepository.updateDocumentTags(documentId, ownerId, List.of("java", "spring"));

            PersistedDocument persistedDocument = findPersistedDocument(documentId);
            assertThat(persistedDocument.tags()).containsExactly("java", "spring");
            assertThat(persistedDocument.fileName()).isEqualTo("tags.md");
            assertThat(persistedDocument.content()).isEqualTo("Document content");
        }

        @Test
        void doesNothingWhenOwnerIdDoesNotMatch() {
            UUID ownerId = insertUser("tags-owner-id", "tags-owner@example.com");
            UUID otherOwnerId = insertUser("other-tags-owner-id", "other-tags-owner@example.com");
            UUID documentId = documentRepository.saveDocument(
                new CreateDocument(ownerId, "tags.md", List.of("unchanged"), "Document content")
            );

            documentRepository.updateDocumentTags(documentId, otherOwnerId, List.of("java", "spring"));

            assertThat(findPersistedDocument(documentId).tags()).containsExactly("unchanged");
        }

        @Test
        void doesNothingWhenDocumentIdDoesNotExist() {
            UUID ownerId = insertUser("missing-tags-owner-id", "missing-tags-owner@example.com");
            UUID documentId = documentRepository.saveDocument(
                new CreateDocument(ownerId, "tags.md", List.of("unchanged"), "Document content")
            );

            documentRepository.updateDocumentTags(UUID.randomUUID(), ownerId, List.of("java", "spring"));

            assertThat(findPersistedDocument(documentId).tags()).containsExactly("unchanged");
        }

        @Test
        void updatesTagsToEmptyList() {
            UUID ownerId = insertUser("empty-tags-owner-id", "empty-tags-owner@example.com");
            UUID documentId = documentRepository.saveDocument(
                new CreateDocument(ownerId, "tags.md", List.of("java", "spring"), "Document content")
            );

            documentRepository.updateDocumentTags(documentId, ownerId, List.of());

            assertThat(findPersistedDocument(documentId).tags()).isEmpty();
        }

    }

    @Nested
    class UpdateDocumentContentAndClearQuestions {

        @Test
        void updatesContentAndClearsQuestionsWhenDocumentIdAndOwnerIdMatch() {
            UUID ownerId = insertUser("update-content-owner-id", "update-content-owner@example.com");
            UUID documentId = documentRepository.saveDocument(
                new CreateDocument(ownerId, "content.md", List.of("java"), "Old content")
            );
            documentRepository.updateDocumentQuestions(documentId, List.of("Question one?", "Question two?"));

            documentRepository.updateDocumentContentAndClearQuestions(documentId, ownerId, "New content");

            PersistedDocument persistedDocument = findPersistedDocument(documentId);
            assertThat(persistedDocument.content()).isEqualTo("New content");
            assertThat(persistedDocument.questions()).isEmpty();
            assertThat(persistedDocument.fileName()).isEqualTo("content.md");
            assertThat(persistedDocument.tags()).containsExactly("java");
        }

        @Test
        void doesNothingWhenOwnerIdDoesNotMatch() {
            UUID ownerId = insertUser("content-owner-id", "content-owner@example.com");
            UUID otherOwnerId = insertUser("other-content-owner-id", "other-content-owner@example.com");
            UUID documentId = documentRepository.saveDocument(
                new CreateDocument(ownerId, "content.md", List.of("java"), "Original content")
            );
            documentRepository.updateDocumentQuestions(documentId, List.of("Original question?"));

            documentRepository.updateDocumentContentAndClearQuestions(documentId, otherOwnerId, "New content");

            PersistedDocument persistedDocument = findPersistedDocument(documentId);
            assertThat(persistedDocument.content()).isEqualTo("Original content");
            assertThat(persistedDocument.questions()).containsExactly("Original question?");
        }

        @Test
        void doesNothingWhenDocumentIdDoesNotExist() {
            UUID ownerId = insertUser("missing-content-owner-id", "missing-content-owner@example.com");
            UUID documentId = documentRepository.saveDocument(
                new CreateDocument(ownerId, "content.md", List.of("java"), "Original content")
            );
            documentRepository.updateDocumentQuestions(documentId, List.of("Original question?"));

            documentRepository.updateDocumentContentAndClearQuestions(UUID.randomUUID(), ownerId, "New content");

            PersistedDocument persistedDocument = findPersistedDocument(documentId);
            assertThat(persistedDocument.content()).isEqualTo("Original content");
            assertThat(persistedDocument.questions()).containsExactly("Original question?");
        }

    }

    @Nested
    class UpdateDocumentQuestions {

        @Test
        void updatesQuestionsWhenDocumentIdExists() {
            UUID ownerId = insertUser("update-questions-owner-id", "update-questions-owner@example.com");
            UUID documentId = documentRepository.saveDocument(
                new CreateDocument(ownerId, "questions.md", List.of("java"), "Document content")
            );

            documentRepository.updateDocumentQuestions(documentId, List.of("Question one?", "Question two?"));

            PersistedDocument persistedDocument = findPersistedDocument(documentId);
            assertThat(persistedDocument.questions()).containsExactly("Question one?", "Question two?");
            assertThat(persistedDocument.content()).isEqualTo("Document content");
            assertThat(persistedDocument.tags()).containsExactly("java");
        }

        @Test
        void replacesExistingQuestions() {
            UUID ownerId = insertUser("replace-questions-owner-id", "replace-questions-owner@example.com");
            UUID documentId = documentRepository.saveDocument(
                new CreateDocument(ownerId, "questions.md", List.of("java"), "Document content")
            );
            documentRepository.updateDocumentQuestions(documentId, List.of("Old question?"));

            documentRepository.updateDocumentQuestions(documentId, List.of("New question?"));

            assertThat(findPersistedDocument(documentId).questions()).containsExactly("New question?");
        }

        @Test
        void updatesQuestionsToEmptyList() {
            UUID ownerId = insertUser("empty-questions-owner-id", "empty-questions-owner@example.com");
            UUID documentId = documentRepository.saveDocument(
                new CreateDocument(ownerId, "questions.md", List.of("java"), "Document content")
            );
            documentRepository.updateDocumentQuestions(documentId, List.of("Question?"));

            documentRepository.updateDocumentQuestions(documentId, List.of());

            assertThat(findPersistedDocument(documentId).questions()).isEmpty();
        }

        @Test
        void doesNothingWhenDocumentIdDoesNotExist() {
            UUID ownerId = insertUser("missing-questions-owner-id", "missing-questions-owner@example.com");
            UUID documentId = documentRepository.saveDocument(
                new CreateDocument(ownerId, "questions.md", List.of("java"), "Document content")
            );

            documentRepository.updateDocumentQuestions(UUID.randomUUID(), List.of("Question?"));

            assertThat(findPersistedDocument(documentId).questions()).isEmpty();
        }

    }

    @Nested
    class DeleteDocument {

        @Test
        void deletesDocumentWhenDocumentIdAndOwnerIdMatch() {
            UUID ownerId = insertUser("delete-document-owner-id", "delete-document-owner@example.com");
            UUID documentId = documentRepository.saveDocument(
                new CreateDocument(ownerId, "delete.md", List.of("java"), "Document content")
            );

            documentRepository.deleteDocument(documentId, ownerId);

            assertThatThrownBy(
                () -> findPersistedDocument(documentId)
            )
                .isInstanceOf(EmptyResultDataAccessException.class);
        }

        @Test
        void doesNothingWhenOwnerIdDoesNotMatch() {
            UUID ownerId = insertUser("delete-owner-id", "delete-owner@example.com");
            UUID otherOwnerId = insertUser("other-delete-owner-id", "other-delete-owner@example.com");
            UUID documentId = documentRepository.saveDocument(
                new CreateDocument(ownerId, "delete.md", List.of("java"), "Document content")
            );

            documentRepository.deleteDocument(documentId, otherOwnerId);

            PersistedDocument persistedDocument = findPersistedDocument(documentId);
            assertThat(persistedDocument.ownerId()).isEqualTo(ownerId);
            assertThat(persistedDocument.fileName()).isEqualTo("delete.md");
        }

        @Test
        void doesNothingWhenDocumentIdDoesNotExist() {
            UUID ownerId = insertUser("missing-delete-document-owner-id", "missing-delete-document-owner@example.com");
            UUID documentId = documentRepository.saveDocument(
                new CreateDocument(ownerId, "delete.md", List.of("java"), "Document content")
            );

            documentRepository.deleteDocument(UUID.randomUUID(), ownerId);

            assertThat(findPersistedDocument(documentId).fileName()).isEqualTo("delete.md");
        }

    }

    @Nested
    class GetDailyMailTargets {

        @Test
        void returnsTargetsForEnabledUsersAtMatchingLocalTime() {
            UUID newYorkOwnerId = insertUser("new-york-daily-mail-owner-id", "new-york-daily-mail-owner@example.com");
            UUID berlinOwnerId = insertUser("berlin-daily-mail-owner-id", "berlin-daily-mail-owner@example.com");
            UUID tokyoOwnerId = insertUser("tokyo-daily-mail-owner-id", "tokyo-daily-mail-owner@example.com");
            updateDailyMailSettings(newYorkOwnerId, "America/New_York", true, LocalTime.of(7, 0), List.of());
            updateDailyMailSettings(berlinOwnerId, "Europe/Berlin", true, LocalTime.of(13, 0), List.of());
            updateDailyMailSettings(tokyoOwnerId, "Asia/Tokyo", true, LocalTime.of(21, 0), List.of());
            UUID newYorkDocumentId = documentRepository.saveDocument(
                new CreateDocument(newYorkOwnerId, "new-york.md", List.of("java"), "New York content")
            );
            UUID berlinDocumentId = documentRepository.saveDocument(
                new CreateDocument(berlinOwnerId, "berlin.md", List.of("java"), "Berlin content")
            );
            UUID tokyoDocumentId = documentRepository.saveDocument(
                new CreateDocument(tokyoOwnerId, "tokyo.md", List.of("java"), "Tokyo content")
            );
            documentRepository.updateDocumentQuestions(newYorkDocumentId, List.of("New York question?"));
            documentRepository.updateDocumentQuestions(berlinDocumentId, List.of("Berlin question?"));
            documentRepository.updateDocumentQuestions(tokyoDocumentId, List.of("Tokyo question?"));

            List<DailyMailTarget> targets = documentRepository.getDailyMailTargets(
                OffsetDateTime.parse("2026-01-15T12:00:00Z")
            );

            assertThat(targets)
                .containsExactlyInAnyOrder(
                    new DailyMailTarget(
                        "new-york-daily-mail-owner@example.com",
                        "new-york.md",
                        "New York content",
                        List.of("New York question?")
                    ),
                    new DailyMailTarget(
                        "berlin-daily-mail-owner@example.com",
                        "berlin.md",
                        "Berlin content",
                        List.of("Berlin question?")
                    ),
                    new DailyMailTarget(
                        "tokyo-daily-mail-owner@example.com",
                        "tokyo.md",
                        "Tokyo content",
                        List.of("Tokyo question?")
                    )
                );
        }

        @Test
        void returnsOnlyDocumentsWithQuestionsMatchingUserDailyMailTags() {
            UUID ownerId = insertUser("tagged-daily-mail-owner-id", "tagged-daily-mail-owner@example.com");
            updateDailyMailSettings(ownerId, "UTC", true, LocalTime.of(6, 0), List.of("spring"));
            UUID matchingDocumentId = documentRepository.saveDocument(
                new CreateDocument(ownerId, "matching.md", List.of("java", "spring"), "Matching content")
            );
            UUID nonMatchingDocumentId = documentRepository.saveDocument(
                new CreateDocument(ownerId, "non-matching.md", List.of("java"), "Non-matching content")
            );
            documentRepository.saveDocument(
                new CreateDocument(ownerId, "without-questions.md", List.of("spring"), "No questions content")
            );
            documentRepository.updateDocumentQuestions(matchingDocumentId, List.of("Matching question?"));
            documentRepository.updateDocumentQuestions(nonMatchingDocumentId, List.of("Non-matching question?"));

            List<DailyMailTarget> targets = documentRepository.getDailyMailTargets(
                OffsetDateTime.parse("2026-01-15T06:00:00Z")
            );

            assertThat(targets)
                .containsExactly(
                    new DailyMailTarget(
                        "tagged-daily-mail-owner@example.com",
                        "matching.md",
                        "Matching content",
                        List.of("Matching question?")
                    )
                );
        }

        @Test
        void returnsEmptyListWhenSoleUserIsDisabled() {
            UUID ownerId = insertUser("disabled-daily-mail-owner-id", "disabled-daily-mail-owner@example.com");
            updateDailyMailSettings(ownerId, "UTC", false, LocalTime.of(6, 0), List.of("java"));
            UUID documentId = documentRepository.saveDocument(
                new CreateDocument(ownerId, "disabled.md", List.of("java"), "Disabled content")
            );
            documentRepository.updateDocumentQuestions(documentId, List.of("Disabled question?"));

            List<DailyMailTarget> targets = documentRepository.getDailyMailTargets(
                OffsetDateTime.parse("2026-01-15T06:00:00Z")
            );

            assertThat(targets).isEmpty();
        }

        @Test
        void returnsEmptyListWhenLocalTimeDoesNotMatch() {
            UUID ownerId = insertUser("wrong-time-daily-mail-owner-id", "wrong-time-daily-mail-owner@example.com");
            updateDailyMailSettings(ownerId, "UTC", true, LocalTime.of(7, 0), List.of());
            UUID documentId = documentRepository.saveDocument(
                new CreateDocument(ownerId, "wrong-time.md", List.of("java"), "Wrong time content")
            );
            documentRepository.updateDocumentQuestions(documentId, List.of("Wrong time question?"));

            List<DailyMailTarget> targets = documentRepository.getDailyMailTargets(
                OffsetDateTime.parse("2026-01-15T06:00:00Z")
            );

            assertThat(targets).isEmpty();
        }

    }

    private UUID insertUser(String authId, String email) {
        return jdbcClient
            .sql(
                """
                INSERT INTO users (
                    auth_id,
                    email
                )
                VALUES (
                    :authId,
                    :email
                )
                RETURNING id;
                """
            )
            .param("authId", authId)
            .param("email", email)
            .query(UUID.class)
            .single();
    }

    private void updateDailyMailSettings(
        UUID userId,
        String timeZone,
        boolean dailyMailEnabled,
        LocalTime dailyMailTime,
        List<String> dailyMailTags
    ) {
        jdbcClient
            .sql(
                """
                UPDATE users SET
                    time_zone = :timeZone,
                    daily_mail_enabled = :dailyMailEnabled,
                    daily_mail_time = :dailyMailTime,
                    daily_mail_tags = :dailyMailTags
                WHERE id = :userId;
                """
            )
            .param("timeZone", timeZone)
            .param("dailyMailEnabled", dailyMailEnabled)
            .param("dailyMailTime", dailyMailTime)
            .param("dailyMailTags", dailyMailTags.toArray(String[]::new))
            .param("userId", userId)
            .update();
    }

    private PersistedDocument findPersistedDocument(UUID id) {
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
                WHERE id = :id;
                """
            )
            .param("id", id)
            .query((rs, rowNum) -> new PersistedDocument(
                rs.getObject("id", UUID.class),
                rs.getObject("owner_id", UUID.class),
                rs.getString("file_name"),
                toStringList(rs.getArray("tags")),
                rs.getString("content"),
                toStringList(rs.getArray("questions"))
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

    private record PersistedDocument(
        UUID id,
        UUID ownerId,
        String fileName,
        List<String> tags,
        String content,
        List<String> questions
    ) {
    }

}
