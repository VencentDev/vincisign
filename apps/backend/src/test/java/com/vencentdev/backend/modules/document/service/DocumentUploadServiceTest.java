package com.vencentdev.backend.modules.document.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vencentdev.backend.common.exception.ForbiddenException;
import com.vencentdev.backend.common.exception.ProjectTransitionException;
import com.vencentdev.backend.modules.auth.AuthenticatedUser;
import com.vencentdev.backend.modules.document.dto.DocumentUploadResponse;
import com.vencentdev.backend.modules.document.entity.Document;
import com.vencentdev.backend.modules.document.entity.DocumentVersion;
import com.vencentdev.backend.modules.document.enums.DocumentVersionStatus;
import com.vencentdev.backend.modules.document.repository.DocumentRepository;
import com.vencentdev.backend.modules.document.storage.DocumentStorage;
import com.vencentdev.backend.modules.document.storage.DocumentStorageObject;
import com.vencentdev.backend.modules.document.validation.DocumentFileValidator;
import com.vencentdev.backend.modules.document.validation.DocumentUploadException;
import com.vencentdev.backend.modules.document.validation.DocumentUploadRejection;
import com.vencentdev.backend.modules.evidence.service.EvidenceEventService;
import com.vencentdev.backend.modules.project.entity.Project;
import com.vencentdev.backend.modules.project.repository.ProjectRepository;
import com.vencentdev.backend.modules.project.service.ProjectTransitionService;
import com.vencentdev.backend.modules.user.service.UserService;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class DocumentUploadServiceTest {

  @Mock private ProjectRepository projects;
  @Mock private DocumentRepository documents;
  @Mock private DocumentVersionService versions;
  @Mock private DocumentFingerprintService fingerprints;
  @Mock private EvidenceEventService evidenceEvents;
  @Mock private ProjectTransitionService projectTransitions;
  @Mock private UserService users;
  @Mock private ObjectProvider<DocumentStorage> storageProvider;
  @Mock private DocumentStorage storage;
  @Mock private DocumentFileValidator validator;

  private DocumentUploadServiceImpl service;
  private UUID projectId;
  private UUID userId;
  private AuthenticatedUser principal;
  private Project project;
  private DocumentFileValidator.ValidatedDocument validated;
  private DocumentStorageObject stored;

  @BeforeEach
  void setUp() {
    service =
        new DocumentUploadServiceImpl(
            projects,
            documents,
            versions,
            fingerprints,
            evidenceEvents,
            projectTransitions,
            users,
            storageProvider,
            validator);
    projectId = UUID.randomUUID();
    userId = UUID.randomUUID();
    principal = new AuthenticatedUser("external-user", "owner@example.com", "Owner", Set.of());
    project =
        Project.builder()
            .id(projectId)
            .organizationId(UUID.randomUUID())
            .ownerUserId(userId)
            .policyId("DOC-POLICY")
            .policyVersion("0.2")
            .build();
    validated =
        new DocumentFileValidator.ValidatedDocument(
            "signed.pdf", "application/pdf", new byte[] {1, 2, 3});
    stored =
        new DocumentStorageObject(
            "vincisign-documents", "documents/opaque/object", "application/pdf", 3);
  }

  @Test
  void storesValidatedBytesBeforePersistingReadyDocumentVersion() {
    MockMultipartFile uploadedFile =
        new MockMultipartFile("file", "signed.pdf", "application/pdf", validated.content());
    Document document =
        Document.builder()
            .id(UUID.randomUUID())
            .projectId(projectId)
            .organizationId(project.getOrganizationId())
            .originalFileName("signed.pdf")
            .originalMediaType("application/pdf")
            .createdByUserId(userId)
            .name("signed.pdf")
            .build();
    DocumentVersion version =
        DocumentVersion.builder()
            .id(UUID.randomUUID())
            .originalFileName("signed.pdf")
            .mediaType("application/pdf")
            .sizeBytes(3)
            .fingerprint("039058c6f2c0cb492c533b0a4d14ef77cc0f78abccced5287d84a1a2011cfb81")
            .status(DocumentVersionStatus.READY)
            .build();
    String fingerprint = version.getFingerprint();
    when(users.resolveInternalId(principal)).thenReturn(userId);
    when(projects.findById(projectId)).thenReturn(Optional.of(project));
    when(validator.validate(uploadedFile)).thenReturn(validated);
    when(fingerprints.sha256(any(byte[].class))).thenReturn(fingerprint);
    when(storageProvider.getIfAvailable()).thenReturn(storage);
    when(storage.store(any())).thenReturn(stored);
    when(documents.saveAndFlush(any(Document.class))).thenReturn(document);
    when(versions.createInitialVersion(any(Document.class), any(DocumentVersion.class)))
        .thenReturn(version);

    DocumentUploadResponse response = service.upload(projectId, principal, uploadedFile);

    assertThat(response.documentId()).isEqualTo(document.getId());
    assertThat(response.documentVersionId()).isEqualTo(version.getId());
    assertThat(response.fingerprint()).isEqualTo(fingerprint);
    assertThat(response.status()).isEqualTo(DocumentVersionStatus.READY);
    ArgumentCaptor<byte[]> contentCaptor = ArgumentCaptor.forClass(byte[].class);
    verify(fingerprints).sha256(contentCaptor.capture());
    assertThat(contentCaptor.getValue()).containsExactly(validated.content());
    ArgumentCaptor<DocumentVersion> versionCaptor = ArgumentCaptor.forClass(DocumentVersion.class);
    verify(versions).createInitialVersion(any(Document.class), versionCaptor.capture());
    assertThat(versionCaptor.getValue().getFingerprint()).isEqualTo(fingerprint);
    assertThat(versionCaptor.getValue().getFingerprintAlgorithm()).isEqualTo("SHA-256");
    verify(storage).store(any());
    verify(storage, never()).delete(any());
    verify(evidenceEvents)
        .recordProjectEvent("document.validation_started", project, userId, principal, java.util.Map.of());
    verify(evidenceEvents)
        .recordDocumentEvent(
            eq("document.uploaded"),
            same(project),
            eq(userId),
            same(principal),
            same(document),
            same(version),
            any());
    verify(projectTransitions)
        .recordDocumentUploaded(same(project), eq(userId), same(principal), same(document), same(version));
    verify(evidenceEvents)
        .recordDocumentEvent(
            eq("document.fingerprinted"),
            same(project),
            eq(userId),
            same(principal),
            same(document),
            same(version),
            any());
    verify(evidenceEvents)
        .recordDocumentEvent(
            eq("document.version_created"),
            same(project),
            eq(userId),
            same(principal),
            same(document),
            same(version),
            any());
    InOrder order = inOrder(storage, documents, versions);
    order.verify(storage).store(any());
    order.verify(documents).saveAndFlush(any(Document.class));
    order.verify(versions).createInitialVersion(any(Document.class), any(DocumentVersion.class));
  }

  @Test
  void rejectsNonOwnerBeforeValidationOrStorage() {
    UUID differentUserId = UUID.randomUUID();
    when(users.resolveInternalId(principal)).thenReturn(differentUserId);
    when(projects.findById(projectId)).thenReturn(Optional.of(project));

    assertThatThrownBy(() -> service.upload(projectId, principal, null))
        .isInstanceOf(ForbiddenException.class);

    verify(validator, never()).validate(any());
    verify(storageProvider, never()).getIfAvailable();
  }

  @Test
  void removesStoredObjectWhenPersistenceFails() {
    MockMultipartFile uploadedFile =
        new MockMultipartFile("file", "signed.pdf", "application/pdf", validated.content());
    when(users.resolveInternalId(principal)).thenReturn(userId);
    when(projects.findById(projectId)).thenReturn(Optional.of(project));
    when(validator.validate(uploadedFile)).thenReturn(validated);
    when(fingerprints.sha256(any(byte[].class))).thenReturn("fingerprint");
    when(storageProvider.getIfAvailable()).thenReturn(storage);
    when(storage.store(any())).thenReturn(stored);
    when(documents.saveAndFlush(any(Document.class)))
        .thenThrow(new IllegalStateException("database unavailable"));

    assertThatThrownBy(() -> service.upload(projectId, principal, uploadedFile))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("database unavailable");

    verify(storage).delete(stored);
  }

  @Test
  void recordsValidationRejectionWithoutPersistingOrStoring() {
    MockMultipartFile uploadedFile =
        new MockMultipartFile("file", "signed.pdf", "application/pdf", new byte[] {1});
    DocumentUploadException rejection =
        new DocumentUploadException(
            DocumentUploadRejection.FILE_TYPE_UNSUPPORTED, "25 MB");
    when(users.resolveInternalId(principal)).thenReturn(userId);
    when(projects.findById(projectId)).thenReturn(Optional.of(project));
    when(validator.validate(uploadedFile)).thenThrow(rejection);

    assertThatThrownBy(() -> service.upload(projectId, principal, uploadedFile))
        .isSameAs(rejection);

    verify(evidenceEvents)
        .recordProjectEventInNewTransaction(
            "document.validation_failed",
            project,
            userId,
            principal,
            DocumentUploadRejection.FILE_TYPE_UNSUPPORTED.code(),
            java.util.Map.of(
                "policyCode", DocumentUploadRejection.FILE_TYPE_UNSUPPORTED.code()));
    verify(storageProvider, never()).getIfAvailable();
    verify(documents, never()).saveAndFlush(any());
  }

  @Test
  void refusesUploadWhenProjectLifecycleDoesNotAllowDocuments() {
    MockMultipartFile uploadedFile =
        new MockMultipartFile("file", "signed.pdf", "application/pdf", validated.content());
    when(users.resolveInternalId(principal)).thenReturn(userId);
    when(projects.findById(projectId)).thenReturn(Optional.of(project));
    doThrow(
            new ProjectTransitionException(
                HttpStatus.CONFLICT,
                "PROJECT_TRANSITION_NOT_ALLOWED",
                "The project cannot accept a document in its current state."))
        .when(projectTransitions)
        .assertDocumentUploadAllowed(project);

    assertThatThrownBy(() -> service.upload(projectId, principal, uploadedFile))
        .isInstanceOf(ProjectTransitionException.class);

    verify(validator, never()).validate(any());
    verify(storageProvider, never()).getIfAvailable();
    verify(evidenceEvents, never())
        .recordProjectEvent("document.validation_started", project, userId, principal, java.util.Map.of());
  }
}
