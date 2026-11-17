package com.vencentdev.backend.modules.document.service;

import com.vencentdev.backend.modules.document.entity.Document;
import com.vencentdev.backend.modules.document.entity.DocumentVersion;
import com.vencentdev.backend.modules.document.enums.DocumentVersionStatus;
import java.util.UUID;

public interface DocumentVersionService {

  DocumentVersion createInitialVersion(Document document, DocumentVersion version);

  DocumentVersion createReplacementVersion(
      UUID organizationId, UUID documentId, DocumentVersion replacement);

  DocumentVersion updateMutableMetadata(
      UUID organizationId,
      UUID versionId,
      String originalFileName,
      String mediaType,
      long sizeBytes,
      String storageBucket,
      String storageObjectKey,
      String fingerprint);

  DocumentVersion lockForSend(UUID organizationId, UUID versionId);

  DocumentVersion transitionToImmutableStatus(
      UUID organizationId, UUID versionId, DocumentVersionStatus immutableStatus);

  void deleteVersion(UUID organizationId, UUID versionId);
}
