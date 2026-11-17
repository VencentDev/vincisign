package com.vencentdev.backend.modules.document.validation;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "app.document.upload")
public class DocumentUploadProperties {

  private DataSize maxFileSize = DataSize.ofMegabytes(25);

  public DataSize getMaxFileSize() {
    return maxFileSize;
  }

  public void setMaxFileSize(DataSize maxFileSize) {
    this.maxFileSize = maxFileSize;
  }

  public long maxFileSizeBytes() {
    if (maxFileSize == null || maxFileSize.toBytes() <= 0) {
      throw new IllegalStateException("Document upload max file size must be positive.");
    }
    return maxFileSize.toBytes();
  }

  public String maxFileSizeLabel() {
    long megabytes = maxFileSizeBytes() / DataSize.ofMegabytes(1).toBytes();
    if (megabytes > 0 && maxFileSizeBytes() % DataSize.ofMegabytes(1).toBytes() == 0) {
      return megabytes + " MB";
    }
    return maxFileSize.toString();
  }
}
