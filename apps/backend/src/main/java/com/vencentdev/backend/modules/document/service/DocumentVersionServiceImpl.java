package com.vencentdev.backend.modules.document.service;

import com.vencentdev.backend.common.exception.ConflictException;
import com.vencentdev.backend.common.exception.ResourceNotFoundException;
import com.vencentdev.backend.modules.document.entity.Document;
import com.vencentdev.backend.modules.document.entity.DocumentVersion;
import com.vencentdev.backend.modules.document.enums.DocumentVersionRole;
import com.vencentdev.backend.modules.document.enums.DocumentVersionStatus;
import com.vencentdev.backend.modules.document.repository.DocumentRepository;
import com.vencentdev.backend.modules.document.repository.DocumentVersionRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentVersionServiceImpl implements DocumentVersionService {

  private final DocumentRepository documents;
  private final DocumentVersionRepository versions;

  public DocumentVersionServiceImpl(
      DocumentRepository documents, DocumentVersionRepository versions) {
    this.documents = documents;
    this.versions = versions;
  }

  @Override
  @Transactional
  public DocumentVersion createInitialVersion(Document document, DocumentVersion version) {
    if (document.getOriginalVersionId() != null || document.getWorkingVersionId() != null) {
      throw new ConflictException("The document already has an initial version");
    }
    version.setDocumentId(document.getId());
    version.setOrganizationId(document.getOrganizationId());
    version.setPredecessorVersionId(null);
    version.setVersionNumber(1);
    version.setRole(DocumentVersionRole.ORIGINAL);
    version.setStatus(DocumentVersionStatus.READY);
    version.setImmutable(false);
    version.setLockedAt(null);
    DocumentVersion saved = versions.saveAndFlush(version);
    document.setOriginalVersionId(saved.getId());
    document.setWorkingVersionId(saved.getId());
    documents.saveAndFlush(document);
    return saved;
  }

  @Override
  @Transactional
  public DocumentVersion createReplacementVersion(
      UUID organizationId, UUID documentId, DocumentVersion replacement) {
    Document document = findDocument(documentId, organizationId);
    DocumentVersion previous = currentWorkingVersion(document);
    ensureMutable(previous);

    int nextVersionNumber =
        versions
            .findTopByDocumentIdAndOrganizationIdOrderByVersionNumberDesc(
                documentId, organizationId)
            .map(version -> version.getVersionNumber() + 1)
            .orElse(1);
    replacement.setDocumentId(documentId);
    replacement.setOrganizationId(document.getOrganizationId());
    replacement.setPredecessorVersionId(previous.getId());
    replacement.setVersionNumber(nextVersionNumber);
    replacement.setRole(DocumentVersionRole.WORKING);
    replacement.setStatus(DocumentVersionStatus.READY);
    replacement.setImmutable(false);
    replacement.setLockedAt(null);
    DocumentVersion saved = versions.saveAndFlush(replacement);
    document.setWorkingVersionId(saved.getId());
    documents.saveAndFlush(document);
    return saved;
  }

  @Override
  @Transactional
  public DocumentVersion updateMutableMetadata(
      UUID organizationId,
      UUID versionId,
      String originalFileName,
      String mediaType,
      long sizeBytes,
      String storageBucket,
      String storageObjectKey,
      String fingerprint) {
    DocumentVersion version = findVersion(versionId, organizationId);
    ensureMutable(version);
    version.setOriginalFileName(originalFileName);
    version.setMediaType(mediaType);
    version.setSizeBytes(sizeBytes);
    version.setStorageBucket(storageBucket);
    version.setStorageObjectKey(storageObjectKey);
    version.setFingerprint(fingerprint);
    return versions.saveAndFlush(version);
  }

  @Override
  @Transactional
  public DocumentVersion lockForSend(UUID organizationId, UUID versionId) {
    DocumentVersion version = findVersion(versionId, organizationId);
    ensureMutable(version);
    if (version.getStatus() != DocumentVersionStatus.READY) {
      throw new ConflictException("Only ready document versions can be locked for send");
    }
    version.setStatus(DocumentVersionStatus.LOCKED_FOR_SEND);
    return versions.saveAndFlush(version);
  }

  @Override
  @Transactional
  public DocumentVersion transitionToImmutableStatus(
      UUID organizationId, UUID versionId, DocumentVersionStatus immutableStatus) {
    if (!isImmutableStatus(immutableStatus)) {
      throw new ConflictException("Only final or archived statuses can lock a document version");
    }
    DocumentVersion version = findVersion(versionId, organizationId);
    ensureMutable(version);
    if (version.getFingerprint() == null || version.getFingerprint().isBlank()) {
      throw new ConflictException("A document version must have a fingerprint before locking");
    }
    version.setStatus(immutableStatus);
    version.setRole(DocumentVersionRole.valueOf(immutableStatus.name()));
    version.setImmutable(true);
    version.setLockedAt(Instant.now());
    return versions.saveAndFlush(version);
  }

  @Override
  @Transactional
  public void deleteVersion(UUID organizationId, UUID versionId) {
    DocumentVersion version = findVersion(versionId, organizationId);
    ensureMutable(version);
    versions.delete(version);
    versions.flush();
  }

  private Document findDocument(UUID documentId, UUID organizationId) {
    return documents
        .findByIdAndOrganizationId(documentId, organizationId)
        .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
  }

  private DocumentVersion findVersion(UUID versionId, UUID organizationId) {
    return versions
        .findByIdAndOrganizationId(versionId, organizationId)
        .orElseThrow(() -> new ResourceNotFoundException("Document version not found"));
  }

  private DocumentVersion currentWorkingVersion(Document document) {
    if (document.getWorkingVersionId() == null) {
      throw new ConflictException("The document has no working version");
    }
    return findVersion(document.getWorkingVersionId(), document.getOrganizationId());
  }

  private void ensureMutable(DocumentVersion version) {
    if (version.isImmutable() || isImmutableStatus(version.getStatus())) {
      throw new ConflictException("The document version is immutable");
    }
  }

  private boolean isImmutableStatus(DocumentVersionStatus status) {
    return status == DocumentVersionStatus.SIGNED_FINAL
        || status == DocumentVersionStatus.NOTARIZED_FINAL
        || status == DocumentVersionStatus.ARCHIVED;
  }
}
