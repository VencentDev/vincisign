package com.vencentdev.backend.modules.project.service;

import com.vencentdev.backend.modules.auth.AuthenticatedUser;
import com.vencentdev.backend.modules.document.entity.Document;
import com.vencentdev.backend.modules.document.entity.DocumentVersion;
import com.vencentdev.backend.modules.project.dto.ProjectTransitionRequest;
import com.vencentdev.backend.modules.project.dto.ProjectTransitionResponse;
import com.vencentdev.backend.modules.project.entity.Project;
import java.util.UUID;

public interface ProjectTransitionService {

  ProjectTransitionResponse transition(
      UUID projectId, AuthenticatedUser principal, ProjectTransitionRequest request);

  ProjectTransitionResponse recordDocumentUploaded(
      Project project,
      UUID actorUserId,
      AuthenticatedUser principal,
      Document document,
      DocumentVersion version);

  void assertDocumentUploadAllowed(Project project);
}
