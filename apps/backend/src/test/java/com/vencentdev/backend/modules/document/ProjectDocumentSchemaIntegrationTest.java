package com.vencentdev.backend.modules.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.vencentdev.backend.IntegrationTestBase;
import com.vencentdev.backend.common.exception.ConflictException;
import com.vencentdev.backend.modules.document.entity.Document;
import com.vencentdev.backend.modules.document.entity.DocumentVersion;
import com.vencentdev.backend.modules.document.enums.DocumentVersionRole;
import com.vencentdev.backend.modules.document.enums.DocumentVersionStatus;
import com.vencentdev.backend.modules.document.repository.DocumentRepository;
import com.vencentdev.backend.modules.document.repository.DocumentVersionRepository;
import com.vencentdev.backend.modules.document.service.DocumentVersionService;
import com.vencentdev.backend.modules.project.entity.Project;
import com.vencentdev.backend.modules.project.enums.ProjectStatus;
import com.vencentdev.backend.modules.project.repository.ProjectRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class ProjectDocumentSchemaIntegrationTest extends IntegrationTestBase {

  private static final String FINGERPRINT =
      "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

  @Autowired private ProjectRepository projects;
  @Autowired private DocumentRepository documents;
  @Autowired private DocumentVersionRepository versions;
  @Autowired private DocumentVersionService versionService;
  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void createsProjectDocumentAndInitialVersionReferences() {
    Aggregate aggregate = createAggregate("initial-version");

    DocumentVersion initial =
        versionService.createInitialVersion(
            aggregate.document(), version("original.pdf", aggregate.userId()));

    Document savedDocument = documents.findById(aggregate.document().getId()).orElseThrow();
    Project savedProject = projects.findById(aggregate.project().getId()).orElseThrow();

    assertThat(savedProject.getStatus()).isEqualTo(ProjectStatus.DRAFT);
    assertThat(initial.getVersionNumber()).isEqualTo(1);
    assertThat(initial.getRole()).isEqualTo(DocumentVersionRole.ORIGINAL);
    assertThat(initial.getStatus()).isEqualTo(DocumentVersionStatus.READY);
    assertThat(savedDocument.getOriginalVersionId()).isEqualTo(initial.getId());
    assertThat(savedDocument.getWorkingVersionId()).isEqualTo(initial.getId());
  }

  @Test
  void replacementCreatesNewVersionAndPreservesPreviousHistory() {
    Aggregate aggregate = createAggregate("replacement-history");
    DocumentVersion initial =
        versionService.createInitialVersion(
            aggregate.document(), version("original.pdf", aggregate.userId()));

    DocumentVersion replacement =
        versionService.createReplacementVersion(
            aggregate.organizationId(),
            aggregate.document().getId(),
            version("replacement.pdf", aggregate.userId()));

    List<DocumentVersion> history =
        versions.findByDocumentIdAndOrganizationIdOrderByVersionNumberAsc(
            aggregate.document().getId(), aggregate.organizationId());
    Document savedDocument = documents.findById(aggregate.document().getId()).orElseThrow();

    assertThat(history).extracting(DocumentVersion::getVersionNumber).containsExactly(1, 2);
    assertThat(history.get(0).getId()).isEqualTo(initial.getId());
    assertThat(history.get(0).getOriginalFileName()).isEqualTo("original.pdf");
    assertThat(replacement.getPredecessorVersionId()).isEqualTo(initial.getId());
    assertThat(replacement.getRole()).isEqualTo(DocumentVersionRole.WORKING);
    assertThat(savedDocument.getOriginalVersionId()).isEqualTo(initial.getId());
    assertThat(savedDocument.getWorkingVersionId()).isEqualTo(replacement.getId());
  }

  @Test
  void serviceRejectsMutationAndDeletionAfterFinalization() {
    Aggregate aggregate = createAggregate("service-immutability");
    DocumentVersion initial =
        versionService.createInitialVersion(
            aggregate.document(), version("original.pdf", aggregate.userId()));

    versionService.transitionToImmutableStatus(
        aggregate.organizationId(), initial.getId(), DocumentVersionStatus.SIGNED_FINAL);

    assertThatThrownBy(
            () ->
                versionService.updateMutableMetadata(
                    aggregate.organizationId(),
                    initial.getId(),
                    "changed.pdf",
                    "application/pdf",
                    99,
                    "documents",
                    "changed.pdf",
                    FINGERPRINT))
        .isInstanceOf(ConflictException.class)
        .hasMessage("The document version is immutable");
    assertThatThrownBy(
            () -> versionService.deleteVersion(aggregate.organizationId(), initial.getId()))
        .isInstanceOf(ConflictException.class)
        .hasMessage("The document version is immutable");
  }

  @Test
  void databaseAllowsFinalizationThenRejectsDirectMutationAndDeletion() {
    Aggregate aggregate = createAggregate("database-immutability");
    DocumentVersion initial =
        versionService.createInitialVersion(
            aggregate.document(), version("original.pdf", aggregate.userId()));

    int finalized =
        jdbcTemplate.update(
            "UPDATE document_versions SET status = 'SIGNED_FINAL', version_role = 'SIGNED_FINAL' "
                + "WHERE id = ?",
            initial.getId());

    assertThat(finalized).isEqualTo(1);
    assertThat(jdbcTemplate.queryForObject(
            "SELECT immutable_version FROM document_versions WHERE id = ?", Boolean.class, initial.getId()))
        .isTrue();

    assertThatThrownBy(
            () ->
                jdbcTemplate.update(
                    "UPDATE document_versions SET original_file_name = 'tampered.pdf' WHERE id = ?",
                    initial.getId()))
        .hasMessageContaining("document version")
        .hasMessageContaining("immutable");
    assertThatThrownBy(
            () -> jdbcTemplate.update("DELETE FROM document_versions WHERE id = ?", initial.getId()))
        .hasMessageContaining("document version")
        .hasMessageContaining("immutable");
  }

  private Aggregate createAggregate(String suffix) {
    UUID userId = UUID.randomUUID();
    UUID organizationId = UUID.randomUUID();
    UUID workspaceId = UUID.randomUUID();
    String unique = suffix + "-" + userId;

    jdbcTemplate.update(
        """
        INSERT INTO users (id, external_id, email, display_name, role, user_type,
                           identity_level, contact_verification_status)
        VALUES (?, ?, ?, ?, 'USER', 'INDIVIDUAL', 'I0', 'UNVERIFIED')
        """,
        userId,
        unique,
        unique + "@example.com",
        "Schema Test User");
    jdbcTemplate.update(
        "INSERT INTO organizations (id, name, owner_user_id) VALUES (?, ?, ?)",
        organizationId,
        "Schema Org " + unique,
        userId);
    jdbcTemplate.update(
        "INSERT INTO workspaces (id, organization_id, name, default_workspace) VALUES (?, ?, ?, TRUE)",
        workspaceId,
        organizationId,
        "Default " + unique);

    Project project =
        projects.saveAndFlush(
            Project.builder()
                .organizationId(organizationId)
                .workspaceId(workspaceId)
                .ownerUserId(userId)
                .name("Project " + unique)
                .build());
    Document document =
        documents.saveAndFlush(
            Document.builder()
                .projectId(project.getId())
                .organizationId(organizationId)
                .name("Document " + unique)
                .originalFileName("original.pdf")
                .originalMediaType("application/pdf")
                .createdByUserId(userId)
                .build());
    return new Aggregate(project, document, organizationId, userId);
  }

  private DocumentVersion version(String fileName, UUID createdByUserId) {
    return DocumentVersion.builder()
        .originalFileName(fileName)
        .mediaType("application/pdf")
        .sizeBytes(128)
        .storageBucket("documents")
        .storageObjectKey("projects/" + UUID.randomUUID() + "/" + fileName)
        .fingerprint(FINGERPRINT)
        .createdByUserId(createdByUserId)
        .build();
  }

  private record Aggregate(Project project, Document document, UUID organizationId, UUID userId) {}
}
