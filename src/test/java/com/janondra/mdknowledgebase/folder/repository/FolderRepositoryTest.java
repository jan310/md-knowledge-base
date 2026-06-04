package com.janondra.mdknowledgebase.folder.repository;

import com.janondra.mdknowledgebase.folder.model.Folder;
import com.janondra.mdknowledgebase.helper.DatabaseIntegrationTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(FolderRepository.class)
class FolderRepositoryTest extends DatabaseIntegrationTest {

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private JdbcClient jdbcClient;

    @Nested
    class CreateFolder {

        @Test
        void createsRootFolder() {
            UUID ownerId = insertUser("create-root-folder-owner-id", "create-root-folder-owner@example.com");

            UUID folderId = folderRepository.createFolder(ownerId, null, "AWS");

            Folder folder = findFolder(folderId);
            assertThat(folder.id()).isEqualTo(folderId);
            assertThat(folder.ownerId()).isEqualTo(ownerId);
            assertThat(folder.parentId()).isNull();
            assertThat(folder.folderName()).isEqualTo("AWS");
        }

        @Test
        void createsNestedFolder() {
            UUID ownerId = insertUser("create-nested-folder-owner-id", "create-nested-folder-owner@example.com");
            UUID parentId = folderRepository.createFolder(ownerId, null, "AWS");

            UUID folderId = folderRepository.createFolder(ownerId, parentId, "IAM");

            Folder folder = findFolder(folderId);
            assertThat(folder.parentId()).isEqualTo(parentId);
            assertThat(folder.folderName()).isEqualTo("IAM");
        }

        @Test
        void throwsExceptionWhenOwnerDoesNotExist() {
            assertThatThrownBy(
                () -> folderRepository.createFolder(UUID.randomUUID(), null, "Orphan")
            )
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void throwsExceptionWhenParentDoesNotExist() {
            UUID ownerId = insertUser("no-parent-folder-owner-id", "no-parent-folder-owner@example.com");

            assertThatThrownBy(
                () -> folderRepository.createFolder(ownerId, UUID.randomUUID(), "Orphan")
            )
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void throwsExceptionWhenSiblingFolderNameAlreadyExists() {
            UUID ownerId = insertUser("duplicate-folder-owner-id", "duplicate-folder-owner@example.com");
            folderRepository.createFolder(ownerId, null, "AWS");

            assertThatThrownBy(
                () -> folderRepository.createFolder(ownerId, null, "AWS")
            )
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void allowsSameFolderNameInDifferentParents() {
            UUID ownerId = insertUser("same-name-folder-owner-id", "same-name-folder-owner@example.com");
            UUID firstParentId = folderRepository.createFolder(ownerId, null, "AWS");
            UUID secondParentId = folderRepository.createFolder(ownerId, null, "Azure");

            UUID firstFolderId = folderRepository.createFolder(ownerId, firstParentId, "IAM");
            UUID secondFolderId = folderRepository.createFolder(ownerId, secondParentId, "IAM");

            assertThat(findFolder(firstFolderId).folderName()).isEqualTo("IAM");
            assertThat(findFolder(secondFolderId).folderName()).isEqualTo("IAM");
        }

    }

    @Nested
    class GetSubFolders {

        @Test
        void returnsRootSubFoldersOrderedByFolderName() {
            UUID ownerId = insertUser("root-sub-folders-owner-id", "root-sub-folders-owner@example.com");
            UUID otherOwnerId = insertUser("other-root-sub-folders-owner-id", "other-root-sub-folders-owner@example.com");
            UUID charlieId = folderRepository.createFolder(ownerId, null, "Charlie");
            UUID alphaId = folderRepository.createFolder(ownerId, null, "Alpha");
            UUID bravoId = folderRepository.createFolder(ownerId, null, "Bravo");
            folderRepository.createFolder(otherOwnerId, null, "Aardvark");

            List<Folder> folders = folderRepository.getSubFolders(ownerId, null);

            assertThat(folders)
                .containsExactly(
                    new Folder(alphaId, ownerId, null, "Alpha"),
                    new Folder(bravoId, ownerId, null, "Bravo"),
                    new Folder(charlieId, ownerId, null, "Charlie")
                );
        }

        @Test
        void returnsNestedSubFoldersForParent() {
            UUID ownerId = insertUser("nested-sub-folders-owner-id", "nested-sub-folders-owner@example.com");
            UUID parentId = folderRepository.createFolder(ownerId, null, "AWS");
            UUID otherParentId = folderRepository.createFolder(ownerId, null, "Azure");
            UUID iamId = folderRepository.createFolder(ownerId, parentId, "IAM");
            UUID laodBalancerId = folderRepository.createFolder(ownerId, parentId, "Load-Balancer");
            folderRepository.createFolder(ownerId, otherParentId, "IAM");

            List<Folder> folders = folderRepository.getSubFolders(ownerId, parentId);

            assertThat(folders)
                .containsExactly(
                    new Folder(iamId, ownerId, parentId, "IAM"),
                    new Folder(laodBalancerId, ownerId, parentId, "Load-Balancer")
                );
        }

        @Test
        void returnsEmptyListWhenFolderHasNoSubFolders() {
            UUID ownerId = insertUser("empty-sub-folders-owner-id", "empty-sub-folders-owner@example.com");
            UUID parentId = folderRepository.createFolder(ownerId, null, "Projects");

            List<Folder> folders = folderRepository.getSubFolders(ownerId, parentId);

            assertThat(folders).isEmpty();
        }

    }

    @Nested
    class RenameFolder {

        @Test
        void renamesFolderWhenFolderIdAndOwnerIdMatch() {
            UUID ownerId = insertUser("rename-folder-owner-id", "rename-folder-owner@example.com");
            UUID folderId = folderRepository.createFolder(ownerId, null, "AWS");

            folderRepository.renameFolder(folderId, ownerId, "Azure");

            assertThat(findFolder(folderId).folderName()).isEqualTo("Azure");
        }

        @Test
        void doesNothingWhenOwnerIdDoesNotMatch() {
            UUID ownerId = insertUser("rename-owner-id", "rename-owner@example.com");
            UUID otherOwnerId = insertUser("other-rename-owner-id", "other-rename-owner@example.com");
            UUID folderId = folderRepository.createFolder(ownerId, null, "AWS");

            folderRepository.renameFolder(folderId, otherOwnerId, "Azure");

            assertThat(findFolder(folderId).folderName()).isEqualTo("AWS");
        }

        @Test
        void throwsExceptionWhenNewNameConflictsWithSibling() {
            UUID ownerId = insertUser("rename-conflict-folder-owner-id", "rename-conflict-folder-owner@example.com");
            folderRepository.createFolder(ownerId, null, "AWS");
            UUID folderId = folderRepository.createFolder(ownerId, null, "Azure");

            assertThatThrownBy(
                () -> folderRepository.renameFolder(folderId, ownerId, "AWS")
            )
                .isInstanceOf(DataIntegrityViolationException.class);
        }

    }

    @Nested
    class MoveFolder {

        @Test
        void movesFolderWhenFolderIdAndOwnerIdMatch() {
            UUID ownerId = insertUser("move-folder-owner-id", "move-folder-owner@example.com");
            UUID oldParentId = folderRepository.createFolder(ownerId, null, "AWS");
            UUID newParentId = folderRepository.createFolder(ownerId, null, "Azure");
            UUID folderId = folderRepository.createFolder(ownerId, oldParentId, "IAM");

            boolean moved = folderRepository.moveFolder(folderId, ownerId, newParentId);

            assertThat(moved).isTrue();
            assertThat(findFolder(folderId).parentId()).isEqualTo(newParentId);
        }

        @Test
        void movesFolderToRoot() {
            UUID ownerId = insertUser("move-folder-root-owner-id", "move-folder-root-owner@example.com");
            UUID parentId = folderRepository.createFolder(ownerId, null, "Cloud");
            UUID folderId = folderRepository.createFolder(ownerId, parentId, "AWS");

            boolean moved = folderRepository.moveFolder(folderId, ownerId, null);

            assertThat(moved).isTrue();
            assertThat(findFolder(folderId).parentId()).isNull();
        }

        @Test
        void doesNothingWhenOwnerIdDoesNotMatch() {
            UUID ownerId = insertUser("move-owner-id", "move-owner@example.com");
            UUID otherOwnerId = insertUser("other-move-owner-id", "other-move-owner@example.com");
            UUID oldParentId = folderRepository.createFolder(ownerId, null, "Old Parent");
            UUID newParentId = folderRepository.createFolder(ownerId, null, "New Parent");
            UUID folderId = folderRepository.createFolder(ownerId, oldParentId, "Child");

            boolean moved = folderRepository.moveFolder(folderId, otherOwnerId, newParentId);

            assertThat(moved).isFalse();
            assertThat(findFolder(folderId).parentId()).isEqualTo(oldParentId);
        }

        @Test
        void doesNothingWhenNewParentDoesNotExist() {
            UUID ownerId = insertUser("missing-move-parent-owner-id", "missing-move-parent-owner@example.com");
            UUID oldParentId = folderRepository.createFolder(ownerId, null, "Old Parent");
            UUID folderId = folderRepository.createFolder(ownerId, oldParentId, "Child");

            boolean moved = folderRepository.moveFolder(folderId, ownerId, UUID.randomUUID());

            assertThat(moved).isFalse();
            assertThat(findFolder(folderId).parentId()).isEqualTo(oldParentId);
        }

        @Test
        void doesNothingWhenNewParentBelongsToDifferentOwner() {
            UUID ownerId = insertUser("different-owner-move-parent-owner-id", "different-owner-move-parent-owner@example.com");
            UUID otherOwnerId = insertUser("other-different-owner-move-parent-owner-id", "other-different-owner-move-parent-owner@example.com");
            UUID oldParentId = folderRepository.createFolder(ownerId, null, "Old Parent");
            UUID folderId = folderRepository.createFolder(ownerId, oldParentId, "Child");
            UUID otherParentId = folderRepository.createFolder(otherOwnerId, null, "Other Parent");

            boolean moved = folderRepository.moveFolder(folderId, ownerId, otherParentId);

            assertThat(moved).isFalse();
            assertThat(findFolder(folderId).parentId()).isEqualTo(oldParentId);
        }

        @Test
        void doesNothingWhenMovingFolderIntoItself() {
            UUID ownerId = insertUser("self-move-owner-id", "self-move-owner@example.com");
            UUID parentId = folderRepository.createFolder(ownerId, null, "Parent");
            UUID folderId = folderRepository.createFolder(ownerId, parentId, "Child");

            boolean moved = folderRepository.moveFolder(folderId, ownerId, folderId);

            assertThat(moved).isFalse();
            assertThat(findFolder(folderId).parentId()).isEqualTo(parentId);
        }

        @Test
        void doesNothingWhenMovingFolderIntoDirectChild() {
            UUID ownerId = insertUser("child-move-owner-id", "child-move-owner@example.com");
            UUID folderId = folderRepository.createFolder(ownerId, null, "Parent");
            UUID childId = folderRepository.createFolder(ownerId, folderId, "Child");

            boolean moved = folderRepository.moveFolder(folderId, ownerId, childId);

            assertThat(moved).isFalse();
            assertThat(findFolder(folderId).parentId()).isNull();
        }

        @Test
        void doesNothingWhenMovingFolderIntoDeepDescendant() {
            UUID ownerId = insertUser("descendant-move-owner-id", "descendant-move-owner@example.com");
            UUID folderId = folderRepository.createFolder(ownerId, null, "Parent");
            UUID childId = folderRepository.createFolder(ownerId, folderId, "Child");
            UUID grandchildId = folderRepository.createFolder(ownerId, childId, "Grandchild");

            boolean moved = folderRepository.moveFolder(folderId, ownerId, grandchildId);

            assertThat(moved).isFalse();
            assertThat(findFolder(folderId).parentId()).isNull();
        }

        @Test
        void movesFolderIntoAncestor() {
            UUID ownerId = insertUser("ancestor-move-owner-id", "ancestor-move-owner@example.com");
            UUID grandparentId = folderRepository.createFolder(ownerId, null, "Grandparent");
            UUID parentId = folderRepository.createFolder(ownerId, grandparentId, "Parent");
            UUID folderId = folderRepository.createFolder(ownerId, parentId, "Child");

            boolean moved = folderRepository.moveFolder(folderId, ownerId, grandparentId);

            assertThat(moved).isTrue();
            assertThat(findFolder(folderId).parentId()).isEqualTo(grandparentId);
        }

        @Test
        void movesFolderIntoSiblingBranch() {
            UUID ownerId = insertUser("sibling-move-owner-id", "sibling-move-owner@example.com");
            UUID parentId = folderRepository.createFolder(ownerId, null, "Parent");
            UUID folderId = folderRepository.createFolder(ownerId, parentId, "Child");
            UUID siblingId = folderRepository.createFolder(ownerId, parentId, "Sibling");
            UUID nephewId = folderRepository.createFolder(ownerId, siblingId, "Nephew");

            boolean moved = folderRepository.moveFolder(folderId, ownerId, nephewId);

            assertThat(moved).isTrue();
            assertThat(findFolder(folderId).parentId()).isEqualTo(nephewId);
        }

        @Test
        void throwsExceptionWhenDestinationHasFolderWithSameName() {
            UUID ownerId = insertUser("move-conflict-folder-owner-id", "move-conflict-folder-owner@example.com");
            UUID oldParentId = folderRepository.createFolder(ownerId, null, "Old Parent");
            UUID newParentId = folderRepository.createFolder(ownerId, null, "New Parent");
            folderRepository.createFolder(ownerId, newParentId, "Child");
            UUID folderId = folderRepository.createFolder(ownerId, oldParentId, "Child");

            assertThatThrownBy(
                () -> folderRepository.moveFolder(folderId, ownerId, newParentId)
            )
                .isInstanceOf(DataIntegrityViolationException.class);
        }

    }

    @Nested
    class DeleteFolder {

        @Test
        void deletesFolderWhenFolderIdAndOwnerIdMatch() {
            UUID ownerId = insertUser("delete-folder-owner-id", "delete-folder-owner@example.com");
            UUID folderId = folderRepository.createFolder(ownerId, null, "Delete");

            folderRepository.deleteFolder(folderId, ownerId);

            assertThatThrownBy(
                () -> findFolder(folderId)
            )
                .isInstanceOf(EmptyResultDataAccessException.class);
        }

        @Test
        void deletesSubFoldersByCascade() {
            UUID ownerId = insertUser("delete-cascade-folder-owner-id", "delete-cascade-folder-owner@example.com");
            UUID folderId = folderRepository.createFolder(ownerId, null, "Delete");
            UUID subFolderId = folderRepository.createFolder(ownerId, folderId, "Child");

            folderRepository.deleteFolder(folderId, ownerId);

            assertThatThrownBy(
                () -> findFolder(subFolderId)
            )
                .isInstanceOf(EmptyResultDataAccessException.class);
        }

        @Test
        void doesNothingWhenOwnerIdDoesNotMatch() {
            UUID ownerId = insertUser("delete-folder-owner-id-mismatch", "delete-folder-owner-mismatch@example.com");
            UUID otherOwnerId = insertUser("other-delete-folder-owner-id", "other-delete-folder-owner@example.com");
            UUID folderId = folderRepository.createFolder(ownerId, null, "Delete");

            folderRepository.deleteFolder(folderId, otherOwnerId);

            assertThat(findFolder(folderId).folderName()).isEqualTo("Delete");
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

    private Folder findFolder(UUID id) {
        return jdbcClient
            .sql(
                """
                SELECT
                    id,
                    owner_id,
                    parent_id,
                    folder_name
                FROM folders
                WHERE id = :id;
                """
            )
            .param("id", id)
            .query((rs, rowNum) -> new Folder(
                rs.getObject("id", UUID.class),
                rs.getObject("owner_id", UUID.class),
                rs.getObject("parent_id", UUID.class),
                rs.getString("folder_name")
            ))
            .single();
    }

}
