package com.vencentdev.backend.modules.document.storage;

import java.util.UUID;

/** Generates object keys without embedding project, organization, or filename identifiers. */
public final class DocumentStorageKeyGenerator {

  public String nextObjectKey() {
    return "documents/%s/%s".formatted(UUID.randomUUID(), UUID.randomUUID());
  }
}
