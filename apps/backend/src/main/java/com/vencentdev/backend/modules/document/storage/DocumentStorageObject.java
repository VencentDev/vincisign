package com.vencentdev.backend.modules.document.storage;

/** Provider-neutral metadata persisted alongside a document version. */
public record DocumentStorageObject(
    String bucket, String objectKey, String mediaType, long sizeBytes) {

  public DocumentStorageObject {
    if (bucket == null || bucket.isBlank()) {
      throw new IllegalArgumentException("Storage bucket is required.");
    }
    if (objectKey == null || objectKey.isBlank() || objectKey.startsWith("/")) {
      throw new IllegalArgumentException("Storage object key is invalid.");
    }
    if (mediaType == null || mediaType.isBlank()) {
      throw new IllegalArgumentException("Storage media type is required.");
    }
    if (sizeBytes < 0) {
      throw new IllegalArgumentException("Storage object size cannot be negative.");
    }
  }
}
