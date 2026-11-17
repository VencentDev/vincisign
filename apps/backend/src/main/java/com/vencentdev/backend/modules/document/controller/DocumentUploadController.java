package com.vencentdev.backend.modules.document.controller;

import com.vencentdev.backend.modules.auth.AuthenticatedUser;
import com.vencentdev.backend.modules.auth.CurrentUser;
import com.vencentdev.backend.modules.document.dto.DocumentUploadResponse;
import com.vencentdev.backend.modules.document.service.DocumentUploadService;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/projects")
public class DocumentUploadController {

  private final DocumentUploadService uploads;

  public DocumentUploadController(DocumentUploadService uploads) {
    this.uploads = uploads;
  }

  @PostMapping(path = "/{projectId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public DocumentUploadResponse upload(
      @CurrentUser AuthenticatedUser user,
      @PathVariable UUID projectId,
      @RequestPart("file") MultipartFile file) {
    return uploads.upload(projectId, user, file);
  }
}
