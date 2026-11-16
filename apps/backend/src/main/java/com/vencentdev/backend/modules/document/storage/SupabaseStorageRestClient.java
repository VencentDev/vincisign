package com.vencentdev.backend.modules.document.storage;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriUtils;

final class SupabaseStorageRestClient implements SupabaseStorageTransport {

  private final RestClient restClient;
  private final URI storageApiUrl;

  SupabaseStorageRestClient(RestClient restClient, URI storageApiUrl) {
    this.restClient = restClient;
    this.storageApiUrl = storageApiUrl;
  }

  @Override
  public void upload(String bucket, String objectKey, byte[] content, String mediaType) {
    restClient
        .post()
        .uri(objectPath("object", bucket, objectKey))
        .contentType(MediaType.parseMediaType(mediaType))
        .header("x-upsert", "false")
        .body(content)
        .retrieve()
        .toBodilessEntity();
  }

  @Override
  public URI createSignedDownloadUrl(String bucket, String objectKey, Duration expiresIn) {
    SignedUrlResponse response =
        restClient
            .post()
            .uri(objectPath("object/sign", bucket, objectKey))
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("expiresIn", expiresIn.toSeconds()))
            .retrieve()
            .body(SignedUrlResponse.class);

    if (response == null || response.signedUrl() == null || response.signedUrl().isBlank()) {
      throw new IllegalStateException("Storage provider returned no signed URL.");
    }
    return resolveSignedUrl(response.signedUrl());
  }

  @Override
  public void delete(String bucket, String objectKey) {
    restClient
        .method(HttpMethod.DELETE)
        .uri(objectPath("object", bucket, null))
        .contentType(MediaType.APPLICATION_JSON)
        .body(Map.of("prefixes", List.of(objectKey)))
        .retrieve()
        .toBodilessEntity();
  }

  private String objectPath(String operation, String bucket, String objectKey) {
    String path =
        "/"
            + operation
            + "/"
            + UriUtils.encodePathSegment(bucket, StandardCharsets.UTF_8);
    if (objectKey != null) {
      path += "/" + UriUtils.encodePath(objectKey, StandardCharsets.UTF_8);
    }
    return path;
  }

  private URI resolveSignedUrl(String signedUrl) {
    if (signedUrl.startsWith("http://") || signedUrl.startsWith("https://")) {
      return URI.create(signedUrl);
    }

    String relativePath = signedUrl.startsWith("/") ? signedUrl.substring(1) : signedUrl;
    return URI.create(storageApiUrl.toString() + relativePath);
  }

  private record SignedUrlResponse(@JsonProperty("signedURL") String signedUrl) {}
}
