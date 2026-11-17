package com.vencentdev.backend.modules.document.storage;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;

/** Supabase implementation of the provider-neutral document storage boundary. */
public final class SupabaseStorageAdapter implements DocumentStorage {

  private final SupabaseStorageTransport transport;
  private final String bucket;
  private final Duration signedDownloadUrlTtl;
  private final DocumentStorageKeyGenerator keyGenerator;

  public SupabaseStorageAdapter(
      SupabaseStorageTransport transport,
      String bucket,
      Duration signedDownloadUrlTtl,
      DocumentStorageKeyGenerator keyGenerator) {
    this.transport = Objects.requireNonNull(transport);
    this.bucket = requireText(bucket, "Storage bucket is required.");
    this.signedDownloadUrlTtl = requirePositive(signedDownloadUrlTtl);
    this.keyGenerator = Objects.requireNonNull(keyGenerator);
  }

  @Override
  public DocumentStorageObject store(DocumentStorageUpload upload) {
    Objects.requireNonNull(upload);
    byte[] content = upload.content();
    String objectKey = keyGenerator.nextObjectKey();

    try {
      transport.upload(bucket, objectKey, content, upload.mediaType());
      return new DocumentStorageObject(bucket, objectKey, upload.mediaType(), content.length);
    } catch (RuntimeException exception) {
      throw DocumentStorageException.forOperation(
          DocumentStorageException.Operation.STORE, exception);
    }
  }

  @Override
  public URI createSignedDownloadUrl(DocumentStorageObject object) {
    ensureBucket(object);

    try {
      URI signedUrl =
          transport.createSignedDownloadUrl(bucket, object.objectKey(), signedDownloadUrlTtl);
      if (signedUrl == null) {
        throw new IllegalStateException("Storage provider returned no signed URL.");
      }
      return signedUrl;
    } catch (RuntimeException exception) {
      throw DocumentStorageException.forOperation(
          DocumentStorageException.Operation.SIGNED_DOWNLOAD, exception);
    }
  }

  @Override
  public void delete(DocumentStorageObject object) {
    ensureBucket(object);

    try {
      transport.delete(bucket, object.objectKey());
    } catch (RuntimeException exception) {
      throw DocumentStorageException.forOperation(
          DocumentStorageException.Operation.DELETE, exception);
    }
  }

  private void ensureBucket(DocumentStorageObject object) {
    Objects.requireNonNull(object);
    if (!bucket.equals(object.bucket())) {
      throw new IllegalArgumentException("Storage object belongs to a different bucket.");
    }
  }

  private static String requireText(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(message);
    }
    return value;
  }

  private static Duration requirePositive(Duration value) {
    Objects.requireNonNull(value, "Signed download URL TTL is required.");
    if (value.isZero() || value.isNegative()) {
      throw new IllegalArgumentException("Signed download URL TTL must be positive.");
    }
    return value;
  }
}
