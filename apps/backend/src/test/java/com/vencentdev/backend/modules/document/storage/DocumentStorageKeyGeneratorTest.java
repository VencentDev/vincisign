package com.vencentdev.backend.modules.document.storage;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DocumentStorageKeyGeneratorTest {

  private final DocumentStorageKeyGenerator generator = new DocumentStorageKeyGenerator();

  @Test
  void generatesOpaqueKeysWithoutCallerIdentifiers() {
    String projectId = "project-7f5f5b27-6d36-42ae-9a12-8e3edbd5c14f";
    String organizationId = "organization-0aab9968-8d85-4b3d-8f2f-5c776bdc89f4";

    String firstKey = generator.nextObjectKey();
    String secondKey = generator.nextObjectKey();

    assertThat(firstKey).matches("documents/[0-9a-f-]{36}/[0-9a-f-]{36}");
    assertThat(secondKey).matches("documents/[0-9a-f-]{36}/[0-9a-f-]{36}");
    assertThat(firstKey).isNotEqualTo(secondKey);
    assertThat(firstKey).doesNotContain(projectId, organizationId);
    assertThat(secondKey).doesNotContain(projectId, organizationId);
  }

  @Test
  void producesDistinctKeysAcrossRepeatedCalls() {
    Set<String> keys = new HashSet<>();

    for (int index = 0; index < 100; index++) {
      keys.add(generator.nextObjectKey());
    }

    assertThat(keys).hasSize(100);
  }
}
