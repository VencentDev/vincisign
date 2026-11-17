package com.vencentdev.backend.modules.document.service;

import com.vencentdev.backend.common.exception.ForbiddenException;
import com.vencentdev.backend.common.exception.ResourceNotFoundException;
import com.vencentdev.backend.modules.auth.AuthenticatedUser;
import com.vencentdev.backend.modules.document.dto.DocumentUploadResponse;
import com.vencentdev.backend.modules.document.entity.Document;
import com.vencentdev.backend.modules.document.entity.DocumentVersion;
import com.vencentdev.backend.modules.document.repository.DocumentRepository;
import com.vencentdev.backend.modules.document.storage.DocumentStorage;
import com.vencentdev.backend.modules.document.storage.DocumentStorageException;
import com.vencentdev.backend.modules.document.storage.DocumentStorageObject;
import com.vencentdev.backend.modules.document.storage.DocumentStorageUpload;
import com.vencentdev.backend.modules.document.validation.DocumentFileValidator;
import com.vencentdev.backend.modules.document.validation.DocumentUploadException;
import com.vencentdev.backend.modules.evidence.service.EvidenceEventService;
import com.vencentdev.backend.modules.project.entity.Project;
import com.vencentdev.backend.modules.project.repository.ProjectRepository;
import com.vencentdev.backend.modules.project.service.ProjectTransitionService;
import com.vencentdev.backend.modules.user.service.UserService;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentUploadServiceImpl implements DocumentUploadService {

  private static final Logger log = LoggerFactory.getLogger(DocumentUploadServiceImpl.class);

  private final ProjectRepository projects;
  private final DocumentRepository documents;
  private final DocumentVersionService versions;
  private final DocumentFingerprintService fingerprints;
  private final EvidenceEventService evidenceEvents;
  private final ProjectTransitionService projectTransitions;
  private final UserService users;
  private final ObjectProvider<DocumentStorage> storageProvider;
  private final DocumentFileValidator validator;

  public DocumentUploadServiceImpl(
      ProjectRepository projects,
      DocumentRepository documents,
      DocumentVersionService versions,
      DocumentFingerprintService fingerprints,
      EvidenceEventService evidenceEvents,
      ProjectTransitionService projectTransitions,
      UserService users,
      ObjectProvider<DocumentStorage> storageProvider,
      DocumentFileValidator validator) {
    this.projects = projects;
    this.documents = documents;
    this.versions = versions;
    this.fingerprints = fingerprints;
    this.evidenceEvents = evidenceEvents;
    this.projectTransitions = projectTransitions;
    this.users = users;
    this.storageProvider = storageProvider;
    this.validator = validator;
  }

  @Override
  @Transactional
  public DocumentUploadResponse upload(
      UUID projectId, AuthenticatedUser principal, MultipartFile uploadedFile) {
    UUID callerUserId = users.resolveInternalId(principal);
    Project project =
        projects
            .findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    if (!callerUserId.equals(project.getOwnerUserId())) {
      throw new ForbiddenException("You do not have access to this project.");
    }
    projectTransitions.assertDocumentUploadAllowed(project);

    evidenceEvents.recordProjectEvent(
        "document.validation_started", project, callerUserId, principal, Map.of());

    DocumentFileValidator.ValidatedDocument validated;
    try {
      validated = validator.validate(uploadedFile);
    } catch (DocumentUploadException exception) {
      evidenceEvents.recordProjectEventInNewTransaction(
          "document.validation_failed",
          project,
          callerUserId,
          principal,
          exception.getPolicyCode(),
          Map.of("policyCode", exception.getPolicyCode()));
      throw exception;
    }
    String fingerprint = fingerprints.sha256(validated.content());
    DocumentStorage storage = storageProvider.getIfAvailable();
    if (storage == null) {
      throw DocumentStorageException.forOperation(
          DocumentStorageException.Operation.STORE,
          new IllegalStateException("Supabase document storage is not configured."));
    }

    DocumentStorageObject stored =
        storage.store(new DocumentStorageUpload(validated.content(), validated.mediaType()));
    try {
      Document document =
          documents.saveAndFlush(
              Document.builder()
                  .projectId(project.getId())
                  .organizationId(project.getOrganizationId())
                  .name(documentName(validated.originalFileName()))
                  .originalFileName(validated.originalFileName())
                  .originalMediaType(stored.mediaType())
                  .createdByUserId(callerUserId)
                  .build());

      DocumentVersion version =
          versions.createInitialVersion(
              document,
              DocumentVersion.builder()
                  .originalFileName(validated.originalFileName())
                  .mediaType(stored.mediaType())
                  .sizeBytes(stored.sizeBytes())
                  .storageBucket(stored.bucket())
                  .storageObjectKey(stored.objectKey())
                  .fingerprint(fingerprint)
                  .createdByUserId(callerUserId)
                  .build());

      evidenceEvents.recordDocumentEvent(
          "document.uploaded",
          project,
          callerUserId,
          principal,
          document,
          version,
          Map.of("mediaType", version.getMediaType(), "sizeBytes", version.getSizeBytes()));
      evidenceEvents.recordDocumentEvent(
          "document.fingerprinted",
          project,
          callerUserId,
          principal,
          document,
          version,
          Map.of(
              "algorithm", version.getFingerprintAlgorithm(),
              "fingerprint", version.getFingerprint()));
      evidenceEvents.recordDocumentEvent(
          "document.version_created",
          project,
          callerUserId,
          principal,
          document,
          version,
          Map.of(
              "versionNumber", version.getVersionNumber(),
              "role", version.getRole().name(),
              "status", version.getStatus().name()));
      projectTransitions.recordDocumentUploaded(
          project, callerUserId, principal, document, version);

      return new DocumentUploadResponse(
          document.getId(),
          version.getId(),
          version.getOriginalFileName(),
          version.getMediaType(),
          version.getSizeBytes(),
          version.getFingerprint(),
          version.getStatus(),
          project.getPolicyId(),
          project.getPolicyVersion());
    } catch (RuntimeException exception) {
      cleanupStoredObject(storage, stored);
      throw exception;
    }
  }

  private void cleanupStoredObject(DocumentStorage storage, DocumentStorageObject stored) {
    try {
      storage.delete(stored);
    } catch (RuntimeException cleanupException) {
      log.warn("Document storage cleanup failed after persistence failure", cleanupException);
    }
  }

  private String documentName(String originalFileName) {
    return originalFileName.length() <= 200
        ? originalFileName
        : originalFileName.substring(0, 200);
  }
}
