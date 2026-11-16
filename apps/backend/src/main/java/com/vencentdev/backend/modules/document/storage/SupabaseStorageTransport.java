package com.vencentdev.backend.modules.document.storage;

import java.net.URI;
import java.time.Duration;

interface SupabaseStorageTransport {

  void upload(String bucket, String objectKey, byte[] content, String mediaType);

  URI createSignedDownloadUrl(String bucket, String objectKey, Duration expiresIn);

  void delete(String bucket, String objectKey);
}
