package com.vencentdev.backend.modules.document.storage;

/** Safe application error for failures while communicating with document storage. */
public class DocumentStorageException extends RuntimeException {

  public enum Operation {
    STORE,
    SIGNED_DOWNLOAD,
    DELETE
  }

  private final Operation operation;

  private DocumentStorageException(Operation operation, Throwable cause) {
    super(safeMessage(operation), cause);
    this.operation = operation;
  }

  static DocumentStorageException forOperation(Operation operation, Throwable cause) {
    return new DocumentStorageException(operation, cause);
  }

  public Operation getOperation() {
    return operation;
  }

  public String getCode() {
    return "DOCUMENT_STORAGE_" + operation.name();
  }

  private static String safeMessage(Operation operation) {
    return switch (operation) {
      case STORE -> "Document storage upload failed.";
      case SIGNED_DOWNLOAD -> "Document storage signed-download URL creation failed.";
      case DELETE -> "Document storage delete failed.";
    };
  }
}
