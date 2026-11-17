package com.vencentdev.backend.modules.document.validation;

import java.io.IOException;
import java.util.Locale;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class DocumentFileValidator {

  private static final String PDF_MEDIA_TYPE = MediaType.APPLICATION_PDF_VALUE;
  private static final int MAX_FILE_NAME_LENGTH = 255;

  private final DocumentUploadProperties properties;

  public DocumentFileValidator(DocumentUploadProperties properties) {
    this.properties = properties;
  }

  public ValidatedDocument validate(MultipartFile file) {
    if (file == null || file.isEmpty() || file.getSize() == 0) {
      throw rejection(DocumentUploadRejection.FILE_EMPTY);
    }
    if (file.getSize() > properties.maxFileSizeBytes()) {
      throw rejection(DocumentUploadRejection.FILE_TOO_LARGE);
    }

    String originalFileName = safeFileName(file.getOriginalFilename());
    if (!hasPdfExtension(originalFileName) || !isPdfMediaType(file.getContentType())) {
      throw rejection(DocumentUploadRejection.FILE_TYPE_UNSUPPORTED);
    }

    byte[] content;
    try {
      content = file.getBytes();
    } catch (IOException exception) {
      throw rejection(DocumentUploadRejection.FILE_CORRUPT, exception);
    }
    if (content.length == 0) {
      throw rejection(DocumentUploadRejection.FILE_EMPTY);
    }
    if (content.length > properties.maxFileSizeBytes()) {
      throw rejection(DocumentUploadRejection.FILE_TOO_LARGE);
    }

    try (PDDocument document = Loader.loadPDF(content)) {
      if (document.isEncrypted()) {
        throw rejection(DocumentUploadRejection.FILE_ENCRYPTION_UNSUPPORTED);
      }
    } catch (InvalidPasswordException exception) {
      throw rejection(DocumentUploadRejection.FILE_PASSWORD_PROTECTED, exception);
    } catch (DocumentUploadException exception) {
      throw exception;
    } catch (IOException | RuntimeException exception) {
      throw rejection(DocumentUploadRejection.FILE_CORRUPT, exception);
    }

    return new ValidatedDocument(originalFileName, PDF_MEDIA_TYPE, content);
  }

  private boolean isPdfMediaType(String contentType) {
    if (contentType == null || contentType.isBlank()) {
      return false;
    }
    try {
      return MediaType.parseMediaType(contentType).isCompatibleWith(MediaType.APPLICATION_PDF);
    } catch (IllegalArgumentException exception) {
      return false;
    }
  }

  private boolean hasPdfExtension(String fileName) {
    return fileName.toLowerCase(Locale.ROOT).endsWith(".pdf");
  }

  private String safeFileName(String originalFileName) {
    if (originalFileName == null || originalFileName.isBlank()) {
      return "document.pdf";
    }
    String normalized = originalFileName.replace('\\', '/');
    int lastSlash = normalized.lastIndexOf('/');
    if (lastSlash >= 0) {
      normalized = normalized.substring(lastSlash + 1);
    }
    normalized = normalized.replaceAll("[\\p{Cntrl}]", "").trim();
    if (normalized.isBlank()) {
      return "document.pdf";
    }
    return normalized.length() <= MAX_FILE_NAME_LENGTH
        ? normalized
        : normalized.substring(0, MAX_FILE_NAME_LENGTH);
  }

  private DocumentUploadException rejection(DocumentUploadRejection rejection) {
    return new DocumentUploadException(rejection, properties.maxFileSizeLabel());
  }

  private DocumentUploadException rejection(
      DocumentUploadRejection rejection, Throwable cause) {
    DocumentUploadException exception = rejection(rejection);
    exception.initCause(cause);
    return exception;
  }

  public record ValidatedDocument(String originalFileName, String mediaType, byte[] content) {
    public ValidatedDocument {
      content = content.clone();
    }

    @Override
    public byte[] content() {
      return content.clone();
    }
  }
}
