package com.vencentdev.backend.modules.document.validation;

public enum DocumentUploadRejection {
  FILE_EMPTY(
      "The uploaded file is empty. Choose a file with content and try again."),
  FILE_TOO_LARGE(null),
  FILE_TYPE_UNSUPPORTED(
      "This file type is not supported for this workflow. Upload a PDF or PDF/A file."),
  FILE_CORRUPT("We could not read this file. It may be damaged."),
  FILE_PASSWORD_PROTECTED("Password-protected files cannot be processed."),
  FILE_ENCRYPTION_UNSUPPORTED("This encryption format cannot be processed.");

  private final String message;

  DocumentUploadRejection(String message) {
    this.message = message;
  }

  public String code() {
    return name();
  }

  public String message(String sizeLimit) {
    if (this == FILE_TOO_LARGE) {
      return "This file exceeds the allowed size of " + sizeLimit + ".";
    }
    return message;
  }
}
