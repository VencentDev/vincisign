package com.vencentdev.backend.modules.document.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class DocumentFingerprintServiceTest {

  private final DocumentFingerprintService fingerprints = new DocumentFingerprintServiceImpl();

  @Test
  void producesTheSameLowercaseSha256ForTheSameBytes() {
    byte[] content = "VinciSign document".getBytes(StandardCharsets.UTF_8);

    String first = fingerprints.sha256(content);
    String second = fingerprints.sha256(content.clone());

    assertThat(first)
        .isEqualTo("be1b0844ea56ac4030010778c142cdd91338411cfeba379ac3c7cbf47c8b5fd4")
        .hasSize(64)
        .isLowerCase();
    assertThat(second).isEqualTo(first);
  }

  @Test
  void producesDifferentFingerprintsWhenAnyByteChanges() {
    byte[] original = "VinciSign document".getBytes(StandardCharsets.UTF_8);
    byte[] changed = "VinciSign documents".getBytes(StandardCharsets.UTF_8);

    assertThat(fingerprints.sha256(original)).isNotEqualTo(fingerprints.sha256(changed));
  }

  @Test
  void rejectsMissingOrEmptyContent() {
    assertThatThrownBy(() -> fingerprints.sha256(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Document content is required.");
    assertThatThrownBy(() -> fingerprints.sha256(new byte[0]))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Document content is required.");
  }
}
