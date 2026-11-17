package com.vencentdev.backend.modules.document.validation;

import com.vencentdev.backend.common.exception.BadRequestException;

public final class DocumentUploadException extends BadRequestException {

  private final String policyCode;

  public DocumentUploadException(DocumentUploadRejection rejection, String sizeLimit) {
    super(rejection.message(sizeLimit));
    this.policyCode = rejection.code();
  }

  public String getPolicyCode() {
    return policyCode;
  }
}
