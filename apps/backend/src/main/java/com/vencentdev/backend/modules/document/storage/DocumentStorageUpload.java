package com.vencentdev.backend.modules.document.storage;

/** Content and metadata required to store one immutable document object. */
public record DocumentStorageUpload(byte[] content, String mediaType) {

  public DocumentStorageUpload {
    if (content == null || content.length == 0) {
      throw new IllegalArgumentException("Storage content cannot be empty.");
    }
    if (mediaType == null || mediaType.isBlank() || mediaType.contains(" ")) {
      throw new IllegalArgumentException("Storage media type is invalid.");
    }
    content = content.clone();
  }

  @Override
  public byte[] content() {
    return content.clone();
  }
}
