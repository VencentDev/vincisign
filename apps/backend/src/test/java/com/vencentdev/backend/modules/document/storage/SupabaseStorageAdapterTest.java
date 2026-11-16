package com.vencentdev.backend.modules.document.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SupabaseStorageAdapterTest {

  private static final String BUCKET = "vincisign-documents";

  private final SupabaseStorageTransport transport = mock(SupabaseStorageTransport.class);
  private final SupabaseStorageAdapter adapter =
      new SupabaseStorageAdapter(
          transport, BUCKET, Duration.ofMinutes(5), new DocumentStorageKeyGenerator());

  @BeforeEach
  void resetTransport() {
    org.mockito.Mockito.reset(transport);
  }

  @Test
  void storesWithAnOpaqueKeyAndReturnsProviderNeutralMetadata() {
    byte[] content = "signed document".getBytes();
    DocumentStorageUpload upload = new DocumentStorageUpload(content, "application/pdf");
    ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<byte[]> contentCaptor = ArgumentCaptor.forClass(byte[].class);

    DocumentStorageObject stored = adapter.store(upload);

    verify(transport)
        .upload(
            eq(BUCKET),
            keyCaptor.capture(),
            contentCaptor.capture(),
            eq("application/pdf"));
    assertThat(stored.bucket()).isEqualTo(BUCKET);
    assertThat(stored.objectKey()).isEqualTo(keyCaptor.getValue());
    assertThat(stored.objectKey()).matches("documents/[0-9a-f-]{36}/[0-9a-f-]{36}");
    assertThat(stored.mediaType()).isEqualTo("application/pdf");
    assertThat(stored.sizeBytes()).isEqualTo(content.length);
    assertThat(contentCaptor.getValue()).containsExactly(content);
  }

  @Test
  void createsSignedDownloadUrlsWithConfiguredTtl() {
    DocumentStorageObject stored =
        new DocumentStorageObject(BUCKET, "documents/random/object", "application/pdf", 42);
    URI signedUrl = URI.create("https://storage.example.test/signed-document");
    when(
            transport.createSignedDownloadUrl(
                eq(BUCKET), eq(stored.objectKey()), eq(Duration.ofMinutes(5))))
        .thenReturn(signedUrl);

    URI actual = adapter.createSignedDownloadUrl(stored);

    assertThat(actual).isEqualTo(signedUrl);
    verify(transport)
        .createSignedDownloadUrl(BUCKET, stored.objectKey(), Duration.ofMinutes(5));
  }

  @Test
  void deletesOnlyObjectsFromTheConfiguredPrivateBucket() {
    DocumentStorageObject stored =
        new DocumentStorageObject(BUCKET, "documents/random/object", "application/pdf", 42);

    adapter.delete(stored);

    verify(transport).delete(BUCKET, stored.objectKey());
  }

  @Test
  void masksProviderFailureWithASafeStorageError() {
    doThrow(new RuntimeException("provider response contains internal details"))
        .when(transport)
        .upload(anyString(), anyString(), any(byte[].class), anyString());

    assertThatThrownBy(
            () -> adapter.store(new DocumentStorageUpload(new byte[] {1}, "application/pdf")))
        .isInstanceOf(DocumentStorageException.class)
        .hasMessage("Document storage upload failed.")
        .extracting(exception -> ((DocumentStorageException) exception).getCode())
        .isEqualTo("DOCUMENT_STORAGE_STORE");
  }

  @Test
  void rejectsReferencesFromAnotherBucketBeforeCallingProvider() {
    DocumentStorageObject stored =
        new DocumentStorageObject(
            "another-private-bucket", "documents/random/object", "application/pdf", 42);

    assertThatThrownBy(() -> adapter.createSignedDownloadUrl(stored))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Storage object belongs to a different bucket.");
  }
}
