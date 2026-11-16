package com.vencentdev.backend.modules.organization.controller;

import com.vencentdev.backend.modules.auth.AuthenticatedUser;
import com.vencentdev.backend.modules.auth.CurrentUser;
import com.vencentdev.backend.modules.organization.dto.AddMembershipRequest;
import com.vencentdev.backend.modules.organization.dto.CreateInvitationRequest;
import com.vencentdev.backend.modules.organization.dto.CreateOrganizationRequest;
import com.vencentdev.backend.modules.organization.dto.CreateWorkspaceRequest;
import com.vencentdev.backend.modules.organization.dto.InvitationResponse;
import com.vencentdev.backend.modules.organization.dto.MembershipResponse;
import com.vencentdev.backend.modules.organization.dto.OrganizationResponse;
import com.vencentdev.backend.modules.organization.dto.UpdateMembershipRequest;
import com.vencentdev.backend.modules.organization.dto.WorkspaceResponse;
import com.vencentdev.backend.modules.organization.service.OrganizationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organizations")
public class OrganizationController {

  private final OrganizationService organizations;

  public OrganizationController(OrganizationService organizations) {
    this.organizations = organizations;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public OrganizationResponse create(
      @CurrentUser AuthenticatedUser user, @Valid @RequestBody CreateOrganizationRequest request) {
    return organizations.create(user, request);
  }

  @GetMapping
  public List<OrganizationResponse> list(@CurrentUser AuthenticatedUser user) {
    return organizations.list(user);
  }

  @GetMapping("/{organizationId}")
  public OrganizationResponse get(
      @CurrentUser AuthenticatedUser user, @PathVariable UUID organizationId) {
    return organizations.get(user, organizationId);
  }

  @PostMapping("/{organizationId}/memberships")
  @ResponseStatus(HttpStatus.CREATED)
  public MembershipResponse addMember(
      @CurrentUser AuthenticatedUser user,
      @PathVariable UUID organizationId,
      @Valid @RequestBody AddMembershipRequest request) {
    return organizations.addOrganizationMember(user, organizationId, request);
  }

  @GetMapping("/{organizationId}/memberships")
  public List<MembershipResponse> listMembers(
      @CurrentUser AuthenticatedUser user, @PathVariable UUID organizationId) {
    return organizations.listOrganizationMembers(user, organizationId);
  }

  @PatchMapping("/{organizationId}/memberships/{membershipId}")
  public MembershipResponse updateMember(
      @CurrentUser AuthenticatedUser user,
      @PathVariable UUID organizationId,
      @PathVariable UUID membershipId,
      @Valid @RequestBody UpdateMembershipRequest request) {
    return organizations.updateOrganizationMember(user, organizationId, membershipId, request);
  }

  @DeleteMapping("/{organizationId}/memberships/{membershipId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void revokeMember(
      @CurrentUser AuthenticatedUser user,
      @PathVariable UUID organizationId,
      @PathVariable UUID membershipId) {
    organizations.revokeOrganizationMember(user, organizationId, membershipId);
  }

  @PostMapping("/{organizationId}/invitations")
  @ResponseStatus(HttpStatus.CREATED)
  public InvitationResponse createInvitation(
      @CurrentUser AuthenticatedUser user,
      @PathVariable UUID organizationId,
      @Valid @RequestBody CreateInvitationRequest request) {
    return organizations.createInvitation(user, organizationId, request);
  }

  @GetMapping("/{organizationId}/invitations")
  public List<InvitationResponse> listInvitations(
      @CurrentUser AuthenticatedUser user, @PathVariable UUID organizationId) {
    return organizations.listInvitations(user, organizationId);
  }

  @DeleteMapping("/{organizationId}/invitations/{invitationId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void revokeInvitation(
      @CurrentUser AuthenticatedUser user,
      @PathVariable UUID organizationId,
      @PathVariable UUID invitationId) {
    organizations.revokeInvitation(user, organizationId, invitationId);
  }

  @PostMapping("/{organizationId}/workspaces")
  @ResponseStatus(HttpStatus.CREATED)
  public WorkspaceResponse createWorkspace(
      @CurrentUser AuthenticatedUser user,
      @PathVariable UUID organizationId,
      @Valid @RequestBody CreateWorkspaceRequest request) {
    return organizations.createWorkspace(user, organizationId, request);
  }

  @GetMapping("/{organizationId}/workspaces")
  public List<WorkspaceResponse> listWorkspaces(
      @CurrentUser AuthenticatedUser user, @PathVariable UUID organizationId) {
    return organizations.listWorkspaces(user, organizationId);
  }

  @GetMapping("/{organizationId}/workspaces/{workspaceId}")
  public WorkspaceResponse getWorkspace(
      @CurrentUser AuthenticatedUser user,
      @PathVariable UUID organizationId,
      @PathVariable UUID workspaceId) {
    return organizations.getWorkspace(user, organizationId, workspaceId);
  }

  @PostMapping("/{organizationId}/workspaces/{workspaceId}/memberships")
  @ResponseStatus(HttpStatus.CREATED)
  public MembershipResponse addWorkspaceMember(
      @CurrentUser AuthenticatedUser user,
      @PathVariable UUID organizationId,
      @PathVariable UUID workspaceId,
      @Valid @RequestBody AddMembershipRequest request) {
    return organizations.addWorkspaceMember(user, organizationId, workspaceId, request);
  }
}
