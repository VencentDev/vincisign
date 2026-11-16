package com.vencentdev.backend.modules.document.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

class DocumentFileValidatorTest {

  private final DocumentUploadProperties properties = new DocumentUploadProperties();
  private final DocumentFileValidator validator = new DocumentFileValidator(properties);

  @Test
  void acceptsReadablePdfAndNormalizesClientFileName() throws Exception {
    MockMultipartFile file = multipart("../../signed.pdf", validPdf(), "application/pdf");

    DocumentFileValidator.ValidatedDocument result = validator.validate(file);

    assertThat(result.originalFileName()).isEqualTo("signed.pdf");
    assertThat(result.mediaType()).isEqualTo("application/pdf");
    assertThat(result.content()).isNotEmpty();
  }

  @Test
  void rejectsEmptyFile() {
    assertRejected(multipart("empty.pdf", new byte[0], "application/pdf"), "FILE_EMPTY");
  }

  @Test
  void rejectsFileOverConfiguredLimitBeforeParsing() throws Exception {
    properties.setMaxFileSize(DataSize.ofBytes(8));

    assertRejected(multipart("large.pdf", validPdf(), "application/pdf"), "FILE_TOO_LARGE");
  }

  @Test
  void rejectsUnsupportedType() {
    assertRejected(
        multipart(
            "document.docx",
            "not a pdf".getBytes(),
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
        "FILE_TYPE_UNSUPPORTED");
  }

  @Test
  void rejectsUnreadablePdf() {
    assertRejected(
        multipart("broken.pdf", "not a pdf".getBytes(), "application/pdf"), "FILE_CORRUPT");
  }

  @Test
  void rejectsPasswordProtectedPdf() throws Exception {
    byte[] protectedPdf = passwordProtectedPdf();

    assertRejected(
        multipart("protected.pdf", protectedPdf, "application/pdf"), "FILE_PASSWORD_PROTECTED");
  }

  private void assertRejected(MockMultipartFile file, String code) {
    assertThatThrownBy(() -> validator.validate(file))
        .isInstanceOfSatisfying(
            DocumentUploadException.class,
            exception -> assertThat(exception.getPolicyCode()).isEqualTo(code));
  }

  private MockMultipartFile multipart(String name, byte[] content, String mediaType) {
    return new MockMultipartFile("file", name, mediaType, content);
  }

  private byte[] validPdf() throws Exception {
    try (PDDocument document = new PDDocument();
        ByteArrayOutputStream output = new ByteArrayOutputStream()) {
      document.save(output);
      return output.toByteArray();
    }
  }

  private byte[] passwordProtectedPdf() throws Exception {
    try (PDDocument document = new PDDocument();
        ByteArrayOutputStream output = new ByteArrayOutputStream()) {
      AccessPermission permission = new AccessPermission();
      StandardProtectionPolicy policy =
          new StandardProtectionPolicy("owner-password", "user-password", permission);
      policy.setEncryptionKeyLength(128);
      document.protect(policy);
      document.save(output);
      return output.toByteArray();
    }
  }
}
