package com.vencentdev.backend.modules.document.service;

import com.vencentdev.backend.modules.auth.AuthenticatedUser;
import com.vencentdev.backend.modules.document.dto.DocumentUploadResponse;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentUploadService {

  DocumentUploadResponse upload(
      UUID projectId, AuthenticatedUser principal, MultipartFile uploadedFile);
}
