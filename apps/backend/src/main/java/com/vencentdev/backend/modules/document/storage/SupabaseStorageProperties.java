package com.vencentdev.backend.modules.document.storage;

import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage.supabase")
public class SupabaseStorageProperties {

  private URI url;
  private String serviceRoleKey;
  private String bucket = "vincisign-documents";
  private Duration signedDownloadUrlTtl = Duration.ofMinutes(5);

  public URI getUrl() {
    return url;
  }

  public void setUrl(URI url) {
    this.url = url;
  }

  public String getServiceRoleKey() {
    return serviceRoleKey;
  }

  public void setServiceRoleKey(String serviceRoleKey) {
    this.serviceRoleKey = serviceRoleKey;
  }

  public String getBucket() {
    return bucket;
  }

  public void setBucket(String bucket) {
    this.bucket = bucket;
  }

  public Duration getSignedDownloadUrlTtl() {
    return signedDownloadUrlTtl;
  }

  public void setSignedDownloadUrlTtl(Duration signedDownloadUrlTtl) {
    this.signedDownloadUrlTtl = signedDownloadUrlTtl;
  }

  URI storageApiUrl() {
    if (url == null || url.toString().isBlank()) {
      throw new IllegalStateException("Supabase Storage URL is not configured.");
    }
    if (serviceRoleKey == null || serviceRoleKey.isBlank()) {
      throw new IllegalStateException("Supabase Storage server credential is not configured.");
    }

    String baseUrl = url.toString().replaceAll("/+$", "");
    return URI.create(baseUrl + "/storage/v1/");
  }
}
