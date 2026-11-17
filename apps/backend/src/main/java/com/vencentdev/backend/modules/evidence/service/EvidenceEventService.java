package com.vencentdev.backend.modules.evidence.service;

import com.vencentdev.backend.modules.auth.AuthenticatedUser;
import com.vencentdev.backend.modules.document.entity.Document;
import com.vencentdev.backend.modules.document.entity.DocumentVersion;
import com.vencentdev.backend.modules.evidence.dto.EvidenceEventResponse;
import com.vencentdev.backend.modules.project.entity.Project;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface EvidenceEventService {

  void recordProjectEvent(
      String eventType,
      Project project,
      UUID actorUserId,
      AuthenticatedUser principal,
      Map<String, Object> payload);

  void recordProjectEventInNewTransaction(
      String eventType,
      Project project,
      UUID actorUserId,
      AuthenticatedUser principal,
      String reasonCode,
      Map<String, Object> payload);

  void recordDocumentEvent(
      String eventType,
      Project project,
      UUID actorUserId,
      AuthenticatedUser principal,
      Document document,
      DocumentVersion version,
      Map<String, Object> payload);

  List<EvidenceEventResponse> listProjectEvents(UUID projectId, AuthenticatedUser principal);
}
