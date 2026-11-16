package com.vencentdev.backend.modules.document.dto;

import com.vencentdev.backend.modules.document.enums.DocumentVersionStatus;
import java.util.UUID;

public record DocumentUploadResponse(
    UUID documentId,
    UUID documentVersionId,
    String originalFileName,
    String mediaType,
    long sizeBytes,
    DocumentVersionStatus status,
    String policyId,
    String policyVersion) {}
