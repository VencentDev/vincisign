package com.vencentdev.backend.modules.evidence.controller;

import com.vencentdev.backend.modules.auth.AuthenticatedUser;
import com.vencentdev.backend.modules.auth.CurrentUser;
import com.vencentdev.backend.modules.evidence.dto.EvidenceEventResponse;
import com.vencentdev.backend.modules.evidence.service.EvidenceEventService;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects")
public class EvidenceEventController {

  private final EvidenceEventService evidenceEvents;

  public EvidenceEventController(EvidenceEventService evidenceEvents) {
    this.evidenceEvents = evidenceEvents;
  }

  @GetMapping("/{projectId}/events")
  public List<EvidenceEventResponse> list(
      @CurrentUser AuthenticatedUser user, @PathVariable UUID projectId) {
    return evidenceEvents.listProjectEvents(projectId, user);
  }
}
