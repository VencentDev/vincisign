package com.vencentdev.backend.modules.document.service;

public interface DocumentFingerprintService {

  String sha256(byte[] content);
}
