package com.vencentdev.backend.modules.document.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.springframework.stereotype.Service;

@Service
public class DocumentFingerprintServiceImpl implements DocumentFingerprintService {

  private static final String SHA_256 = "SHA-256";

  @Override
  public String sha256(byte[] content) {
    if (content == null || content.length == 0) {
      throw new IllegalArgumentException("Document content is required.");
    }

    try {
      byte[] digest = MessageDigest.getInstance(SHA_256).digest(content);
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException(
          "SHA-256 is not available in the current Java runtime.", exception);
    }
  }
}
