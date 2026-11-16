package com.vencentdev.backend.modules.organization.controller;

import com.vencentdev.backend.modules.auth.AuthenticatedUser;
import com.vencentdev.backend.modules.auth.CurrentUser;
import com.vencentdev.backend.modules.organization.dto.AcceptInvitationRequest;
import com.vencentdev.backend.modules.organization.dto.MembershipResponse;
import com.vencentdev.backend.modules.organization.service.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/invitations")
@PreAuthorize("@onboardingAccessGuard.isComplete(authentication)")
public class InvitationController {

  private final OrganizationService organizations;

  public InvitationController(OrganizationService organizations) {
    this.organizations = organizations;
  }

  @PostMapping("/accept")
  public MembershipResponse accept(
      @CurrentUser AuthenticatedUser user, @Valid @RequestBody AcceptInvitationRequest request) {
    return organizations.acceptInvitation(user, request);
  }
}
