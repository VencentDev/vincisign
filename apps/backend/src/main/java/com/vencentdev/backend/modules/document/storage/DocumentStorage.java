package com.vencentdev.backend.modules.document.storage;

import java.net.URI;

/** Provider-neutral boundary for document bytes and their storage references. */
public interface DocumentStorage {

  DocumentStorageObject store(DocumentStorageUpload upload);

  URI createSignedDownloadUrl(DocumentStorageObject object);

  void delete(DocumentStorageObject object);
}
