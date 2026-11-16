package com.vencentdev.backend.modules.organization.service;

import com.vencentdev.backend.modules.auth.AuthenticatedUser;
import com.vencentdev.backend.modules.organization.dto.AcceptInvitationRequest;
import com.vencentdev.backend.modules.organization.dto.AddMembershipRequest;
import com.vencentdev.backend.modules.organization.dto.CreateInvitationRequest;
import com.vencentdev.backend.modules.organization.dto.CreateOrganizationRequest;
import com.vencentdev.backend.modules.organization.dto.CreateWorkspaceRequest;
import com.vencentdev.backend.modules.organization.dto.InvitationResponse;
import com.vencentdev.backend.modules.organization.dto.MembershipResponse;
import com.vencentdev.backend.modules.organization.dto.OrganizationResponse;
import com.vencentdev.backend.modules.organization.dto.UpdateMembershipRequest;
import com.vencentdev.backend.modules.organization.dto.WorkspaceResponse;
import java.util.List;
import java.util.UUID;

public interface OrganizationService {

  OrganizationResponse create(AuthenticatedUser principal, CreateOrganizationRequest request);

  List<OrganizationResponse> list(AuthenticatedUser principal);

  OrganizationResponse get(AuthenticatedUser principal, UUID organizationId);

  WorkspaceResponse createWorkspace(
      AuthenticatedUser principal, UUID organizationId, CreateWorkspaceRequest request);

  List<WorkspaceResponse> listWorkspaces(AuthenticatedUser principal, UUID organizationId);

  WorkspaceResponse getWorkspace(
      AuthenticatedUser principal, UUID organizationId, UUID workspaceId);

  MembershipResponse addOrganizationMember(
      AuthenticatedUser principal, UUID organizationId, AddMembershipRequest request);

  MembershipResponse addWorkspaceMember(
      AuthenticatedUser principal,
      UUID organizationId,
      UUID workspaceId,
      AddMembershipRequest request);

  List<MembershipResponse> listOrganizationMembers(
      AuthenticatedUser principal, UUID organizationId);

  MembershipResponse updateOrganizationMember(
      AuthenticatedUser principal,
      UUID organizationId,
      UUID membershipId,
      UpdateMembershipRequest request);

  void revokeOrganizationMember(
      AuthenticatedUser principal, UUID organizationId, UUID membershipId);

  InvitationResponse createInvitation(
      AuthenticatedUser principal, UUID organizationId, CreateInvitationRequest request);

  List<InvitationResponse> listInvitations(AuthenticatedUser principal, UUID organizationId);

  void revokeInvitation(AuthenticatedUser principal, UUID organizationId, UUID invitationId);

  MembershipResponse acceptInvitation(AuthenticatedUser principal, AcceptInvitationRequest request);
}
