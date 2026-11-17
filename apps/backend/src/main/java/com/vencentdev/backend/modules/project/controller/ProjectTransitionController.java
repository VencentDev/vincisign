package com.vencentdev.backend.modules.project.controller;

import com.vencentdev.backend.modules.auth.AuthenticatedUser;
import com.vencentdev.backend.modules.auth.CurrentUser;
import com.vencentdev.backend.modules.project.dto.ProjectTransitionRequest;
import com.vencentdev.backend.modules.project.dto.ProjectTransitionResponse;
import com.vencentdev.backend.modules.project.service.ProjectTransitionService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectTransitionController {

  private final ProjectTransitionService transitions;

  public ProjectTransitionController(ProjectTransitionService transitions) {
    this.transitions = transitions;
  }

  @PostMapping("/{projectId}/transitions")
  public ProjectTransitionResponse transition(
      @CurrentUser AuthenticatedUser user,
      @PathVariable UUID projectId,
      @Valid @RequestBody ProjectTransitionRequest request) {
    return transitions.transition(projectId, user, request);
  }
}
