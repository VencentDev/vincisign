package com.vencentdev.backend.modules.document.enums;

public enum DocumentVersionStatus {
  RECEIVED,
  VALIDATING,
  SCAN_PENDING,
  NORMALIZING,
  READY,
  QUARANTINED,
  SIGNED_FINAL,
  NOTARIZED_FINAL,
  ARCHIVED,
  REJECTED,
  FAILED,
  LOCKED_FOR_SEND
}
