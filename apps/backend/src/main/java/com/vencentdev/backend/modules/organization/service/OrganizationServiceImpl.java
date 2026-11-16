package com.vencentdev.backend.modules.organization.service;

import com.vencentdev.backend.common.exception.BadRequestException;
import com.vencentdev.backend.common.exception.ConflictException;
import com.vencentdev.backend.common.exception.ForbiddenException;
import com.vencentdev.backend.common.exception.ResourceNotFoundException;
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
import com.vencentdev.backend.modules.organization.entity.MembershipInvitation;
import com.vencentdev.backend.modules.organization.entity.Organization;
import com.vencentdev.backend.modules.organization.entity.OrganizationMembership;
import com.vencentdev.backend.modules.organization.entity.Workspace;
import com.vencentdev.backend.modules.organization.entity.WorkspaceMembership;
import com.vencentdev.backend.modules.organization.enums.InvitationStatus;
import com.vencentdev.backend.modules.organization.enums.MembershipStatus;
import com.vencentdev.backend.modules.organization.enums.OrganizationPermission;
import com.vencentdev.backend.modules.organization.enums.OrganizationRole;
import com.vencentdev.backend.modules.organization.repository.MembershipInvitationRepository;
import com.vencentdev.backend.modules.organization.repository.OrganizationMembershipRepository;
import com.vencentdev.backend.modules.organization.repository.OrganizationRepository;
import com.vencentdev.backend.modules.organization.repository.WorkspaceMembershipRepository;
import com.vencentdev.backend.modules.organization.repository.WorkspaceRepository;
import com.vencentdev.backend.modules.user.entity.User;
import com.vencentdev.backend.modules.user.repository.UserRepository;
import com.vencentdev.backend.modules.user.service.UserService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationServiceImpl implements OrganizationService {

  private static final Duration INVITATION_TTL = Duration.ofDays(7);

  private final OrganizationRepository organizations;
  private final WorkspaceRepository workspaces;
  private final OrganizationMembershipRepository organizationMemberships;
  private final WorkspaceMembershipRepository workspaceMemberships;
  private final MembershipInvitationRepository invitations;
  private final UserRepository users;
  private final UserService userService;
  private final SecureRandom secureRandom = new SecureRandom();

  public OrganizationServiceImpl(
      OrganizationRepository organizations,
      WorkspaceRepository workspaces,
      OrganizationMembershipRepository organizationMemberships,
      WorkspaceMembershipRepository workspaceMemberships,
      MembershipInvitationRepository invitations,
      UserRepository users,
      UserService userService) {
    this.organizations = organizations;
    this.workspaces = workspaces;
    this.organizationMemberships = organizationMemberships;
    this.workspaceMemberships = workspaceMemberships;
    this.invitations = invitations;
    this.users = users;
    this.userService = userService;
  }

  @Override
  @Transactional
  public OrganizationResponse create(
      AuthenticatedUser principal, CreateOrganizationRequest request) {
    UUID userId = userService.resolveInternalId(principal);
    Organization organization =
        organizations.save(
            Organization.builder()
                .name(request.name().trim())
                .jurisdictionProfile("PH")
                .ownerUserId(userId)
                .build());

    Workspace defaultWorkspace =
        workspaces.save(
            Workspace.builder()
                .organizationId(organization.getId())
                .name("Default")
                .defaultWorkspace(true)
                .build());

    organizationMemberships.save(
        OrganizationMembership.builder()
            .organizationId(organization.getId())
            .userId(userId)
            .status(MembershipStatus.ACTIVE)
            .role(OrganizationRole.ORGANIZATION_OWNER)
            .build());
    workspaceMemberships.save(
        WorkspaceMembership.builder()
            .workspaceId(defaultWorkspace.getId())
            .userId(userId)
            .status(MembershipStatus.ACTIVE)
            .build());

    return toResponse(organization, userId);
  }

  @Override
  @Transactional
  public List<OrganizationResponse> list(AuthenticatedUser principal) {
    UUID userId = userService.resolveInternalId(principal);
    return organizationMemberships
        .findByUserIdAndStatusOrderByCreatedAtAsc(userId, MembershipStatus.ACTIVE)
        .stream()
        .map(membership -> organizations.findById(membership.getOrganizationId()))
        .flatMap(java.util.Optional::stream)
        .map(organization -> toResponse(organization, userId))
        .toList();
  }

  @Override
  @Transactional
  public OrganizationResponse get(AuthenticatedUser principal, UUID organizationId) {
    UUID userId = userService.resolveInternalId(principal);
    Organization organization = findOrganization(organizationId);
    requireOrganizationMember(organizationId, userId);
    return toResponse(organization, userId);
  }

  @Override
  @Transactional
  public WorkspaceResponse createWorkspace(
      AuthenticatedUser principal, UUID organizationId, CreateWorkspaceRequest request) {
    UUID userId = userService.resolveInternalId(principal);
    findOrganization(organizationId);
    requirePermission(organizationId, userId, OrganizationPermission.WORKSPACE_MANAGE);

    if (workspaces.existsByOrganizationIdAndNameIgnoreCase(organizationId, request.name().trim())) {
      throw new ConflictException("Workspace name already exists");
    }

    Workspace workspace =
        workspaces.save(
            Workspace.builder()
                .organizationId(organizationId)
                .name(request.name().trim())
                .defaultWorkspace(false)
                .build());
    ensureWorkspaceMembership(workspace.getId(), userId);
    return toResponse(workspace);
  }

  @Override
  @Transactional
  public List<WorkspaceResponse> listWorkspaces(AuthenticatedUser principal, UUID organizationId) {
    UUID userId = userService.resolveInternalId(principal);
    Organization organization = findOrganization(organizationId);
    requireOrganizationMember(organizationId, userId);
    return visibleWorkspaces(organization, userId).stream().map(this::toResponse).toList();
  }

  @Override
  @Transactional
  public WorkspaceResponse getWorkspace(
      AuthenticatedUser principal, UUID organizationId, UUID workspaceId) {
    UUID userId = userService.resolveInternalId(principal);
    Organization organization = findOrganization(organizationId);
    requireOrganizationMember(organizationId, userId);
    Workspace workspace = findWorkspace(organizationId, workspaceId);
    if (!isOrganizationOwner(organization, userId)
        && !workspaceMemberships.existsByWorkspaceIdAndUserIdAndStatus(
            workspaceId, userId, MembershipStatus.ACTIVE)) {
      throw new ResourceNotFoundException("Workspace not found");
    }
    return toResponse(workspace);
  }

  @Override
  @Transactional
  public MembershipResponse addOrganizationMember(
      AuthenticatedUser principal, UUID organizationId, AddMembershipRequest request) {
    UUID actorId = userService.resolveInternalId(principal);
    requirePermission(organizationId, actorId, OrganizationPermission.MEMBERSHIP_MANAGE);
    User target = findUserByEmail(request.email());
    OrganizationRole role =
        request.role() == null ? OrganizationRole.ORGANIZATION_MEMBER : request.role();
    validateAssignedRole(actorId, organizationId, role);
    OrganizationMembership membership =
        activateOrganizationMembership(organizationId, target, role);
    Workspace defaultWorkspace =
        workspaces
            .findByOrganizationIdAndDefaultWorkspaceTrue(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Default workspace not found"));
    ensureWorkspaceMembership(defaultWorkspace.getId(), target.getId());
    return toMembershipResponse(membership, target);
  }

  @Override
  @Transactional
  public MembershipResponse addWorkspaceMember(
      AuthenticatedUser principal,
      UUID organizationId,
      UUID workspaceId,
      AddMembershipRequest request) {
    UUID actorId = userService.resolveInternalId(principal);
    requirePermission(organizationId, actorId, OrganizationPermission.MEMBERSHIP_MANAGE);
    Workspace workspace = findWorkspace(organizationId, workspaceId);
    User target = findUserByEmail(request.email());
    requireOrganizationMember(organizationId, target.getId());
    WorkspaceMembership membership = ensureWorkspaceMembership(workspace.getId(), target.getId());
    OrganizationMembership organizationMembership =
        findActiveMembership(organizationId, target.getId());
    return toMembershipResponse(
        membership, target, organizationId, organizationMembership.getRole());
  }

  @Override
  @Transactional
  public List<MembershipResponse> listOrganizationMembers(
      AuthenticatedUser principal, UUID organizationId) {
    UUID actorId = userService.resolveInternalId(principal);
    requirePermission(organizationId, actorId, OrganizationPermission.MEMBERSHIP_VIEW);
    return organizationMemberships.findByOrganizationId(organizationId).stream()
        .map(membership -> toMembershipResponse(membership, findUserById(membership.getUserId())))
        .toList();
  }

  @Override
  @Transactional
  public MembershipResponse updateOrganizationMember(
      AuthenticatedUser principal,
      UUID organizationId,
      UUID membershipId,
      UpdateMembershipRequest request) {
    UUID actorId = userService.resolveInternalId(principal);
    requireOwner(organizationId, actorId);
    OrganizationMembership membership = findMembership(organizationId, membershipId);
    validateInvitedRole(request.role());
    if (membership.getRole() == OrganizationRole.ORGANIZATION_OWNER) {
      throw new ForbiddenException("The organization owner role cannot be reassigned");
    }
    membership.setRole(request.role());
    return toMembershipResponse(membership, findUserById(membership.getUserId()));
  }

  @Override
  @Transactional
  public void revokeOrganizationMember(
      AuthenticatedUser principal, UUID organizationId, UUID membershipId) {
    UUID actorId = userService.resolveInternalId(principal);
    OrganizationMembership actor =
        requirePermission(organizationId, actorId, OrganizationPermission.MEMBERSHIP_MANAGE);
    Organization organization = findOrganization(organizationId);
    OrganizationMembership membership = findMembership(organizationId, membershipId);
    if (membership.getRole() == OrganizationRole.ORGANIZATION_OWNER
        || membership.getUserId().equals(organization.getOwnerUserId())
        || (actor.getRole() != OrganizationRole.ORGANIZATION_OWNER
            && membership.getRole() == OrganizationRole.ORGANIZATION_ADMINISTRATOR)) {
      throw new ForbiddenException("This membership cannot be revoked by the current actor");
    }
    membership.setStatus(MembershipStatus.REVOKED);
    workspaceMemberships.findByUserId(membership.getUserId()).stream()
        .filter(
            workspaceMembership ->
                workspaces
                    .findById(workspaceMembership.getWorkspaceId())
                    .map(workspace -> workspace.getOrganizationId().equals(organizationId))
                    .orElse(false))
        .forEach(workspaceMembership -> workspaceMembership.setStatus(MembershipStatus.REVOKED));
  }

  @Override
  @Transactional
  public InvitationResponse createInvitation(
      AuthenticatedUser principal, UUID organizationId, CreateInvitationRequest request) {
    UUID actorId = userService.resolveInternalId(principal);
    requirePermission(organizationId, actorId, OrganizationPermission.MEMBERSHIP_MANAGE);
    validateInvitedRole(request.role());
    if (request.workspaceId() != null) {
      findWorkspace(organizationId, request.workspaceId());
    }
    String email = normalizeEmail(request.email());
    if (hasActiveMembership(organizationId, email)) {
      throw new ConflictException("Account is already an organization member");
    }
    boolean pendingInvitationExists =
        invitations
            .findByOrganizationIdAndStatusOrderByCreatedAtDesc(
                organizationId, InvitationStatus.PENDING)
            .stream()
            .anyMatch(
                invitation ->
                    invitation.getEmail().equalsIgnoreCase(email)
                        && Objects.equals(invitation.getWorkspaceId(), request.workspaceId()));
    if (pendingInvitationExists) {
      throw new ConflictException("A pending invitation already exists");
    }

    String token = generateToken();
    MembershipInvitation invitation =
        invitations.save(
            MembershipInvitation.builder()
                .organizationId(organizationId)
                .workspaceId(request.workspaceId())
                .email(email)
                .role(request.role())
                .tokenHash(hashToken(token))
                .status(InvitationStatus.PENDING)
                .expiresAt(Instant.now().plus(INVITATION_TTL))
                .invitedByUserId(actorId)
                .build());
    return toInvitationResponse(invitation, token);
  }

  @Override
  @Transactional
  public List<InvitationResponse> listInvitations(
      AuthenticatedUser principal, UUID organizationId) {
    UUID actorId = userService.resolveInternalId(principal);
    requirePermission(organizationId, actorId, OrganizationPermission.MEMBERSHIP_VIEW);
    Instant now = Instant.now();
    return invitations
        .findByOrganizationIdAndStatusOrderByCreatedAtDesc(organizationId, InvitationStatus.PENDING)
        .stream()
        .map(invitation -> expireIfNecessary(invitation, now))
        .filter(invitation -> invitation.getStatus() == InvitationStatus.PENDING)
        .map(invitation -> toInvitationResponse(invitation, null))
        .toList();
  }

  @Override
  @Transactional
  public void revokeInvitation(
      AuthenticatedUser principal, UUID organizationId, UUID invitationId) {
    UUID actorId = userService.resolveInternalId(principal);
    requirePermission(organizationId, actorId, OrganizationPermission.MEMBERSHIP_MANAGE);
    MembershipInvitation invitation =
        invitations
            .findByIdAndOrganizationId(invitationId, organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));
    if (invitation.getStatus() != InvitationStatus.PENDING) {
      throw new ConflictException("Only pending invitations can be revoked");
    }
    invitation.setStatus(InvitationStatus.REVOKED);
    invitation.setRevokedAt(Instant.now());
  }

  @Override
  @Transactional
  public MembershipResponse acceptInvitation(
      AuthenticatedUser principal, AcceptInvitationRequest request) {
    MembershipInvitation invitation =
        invitations
            .findByTokenHash(hashToken(request.token()))
            .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));
    Instant now = Instant.now();
    expireIfNecessary(invitation, now);
    if (invitation.getStatus() != InvitationStatus.PENDING) {
      throw new ConflictException("Invitation is no longer available");
    }
    if (principal.email() == null || !principal.email().equalsIgnoreCase(invitation.getEmail())) {
      throw new ForbiddenException("Invitation email does not match the authenticated account");
    }

    UUID userId = userService.resolveInternalId(principal);
    User target = findUserById(userId);
    OrganizationMembership membership =
        activateOrganizationMembership(
            invitation.getOrganizationId(), target, invitation.getRole());
    Workspace defaultWorkspace =
        workspaces
            .findByOrganizationIdAndDefaultWorkspaceTrue(invitation.getOrganizationId())
            .orElseThrow(() -> new ResourceNotFoundException("Default workspace not found"));
    ensureWorkspaceMembership(defaultWorkspace.getId(), userId);
    if (invitation.getWorkspaceId() != null) {
      findWorkspace(invitation.getOrganizationId(), invitation.getWorkspaceId());
      ensureWorkspaceMembership(invitation.getWorkspaceId(), userId);
    }
    invitation.setStatus(InvitationStatus.ACCEPTED);
    invitation.setAcceptedByUserId(userId);
    invitation.setAcceptedAt(now);
    return toMembershipResponse(membership, target);
  }

  private Organization findOrganization(UUID organizationId) {
    return organizations
        .findById(organizationId)
        .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
  }

  private Workspace findWorkspace(UUID organizationId, UUID workspaceId) {
    return workspaces
        .findById(workspaceId)
        .filter(workspace -> workspace.getOrganizationId().equals(organizationId))
        .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
  }

  private User findUserByEmail(String email) {
    return users
        .findByEmailIgnoreCase(normalizeEmail(email))
        .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
  }

  private User findUserById(UUID userId) {
    return users
        .findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
  }

  private OrganizationMembership findActiveMembership(UUID organizationId, UUID userId) {
    return organizationMemberships
        .findByOrganizationIdAndUserId(organizationId, userId)
        .filter(membership -> membership.getStatus() == MembershipStatus.ACTIVE)
        .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
  }

  private OrganizationMembership findMembership(UUID organizationId, UUID membershipId) {
    return organizationMemberships
        .findById(membershipId)
        .filter(membership -> membership.getOrganizationId().equals(organizationId))
        .orElseThrow(() -> new ResourceNotFoundException("Membership not found"));
  }

  private void requireOrganizationMember(UUID organizationId, UUID userId) {
    findActiveMembership(organizationId, userId);
  }

  private OrganizationMembership requirePermission(
      UUID organizationId, UUID userId, OrganizationPermission permission) {
    OrganizationMembership membership = findActiveMembership(organizationId, userId);
    if (!RolePermissionCatalog.organizationPermissions(membership.getRole()).contains(permission)) {
      throw new ResourceNotFoundException("Organization not found");
    }
    return membership;
  }

  private void requireOwner(UUID organizationId, UUID userId) {
    Organization organization = findOrganization(organizationId);
    OrganizationMembership membership = findActiveMembership(organizationId, userId);
    if (!organization.getOwnerUserId().equals(userId)
        || membership.getRole() != OrganizationRole.ORGANIZATION_OWNER) {
      throw new ResourceNotFoundException("Organization not found");
    }
  }

  private boolean isOrganizationOwner(Organization organization, UUID userId) {
    return organization.getOwnerUserId().equals(userId);
  }

  private List<Workspace> visibleWorkspaces(Organization organization, UUID userId) {
    List<Workspace> organizationWorkspaces =
        workspaces.findByOrganizationIdOrderByCreatedAtAsc(organization.getId());
    if (isOrganizationOwner(organization, userId)) {
      return organizationWorkspaces;
    }
    Set<UUID> workspaceIds =
        workspaceMemberships.findByUserIdAndStatus(userId, MembershipStatus.ACTIVE).stream()
            .map(WorkspaceMembership::getWorkspaceId)
            .collect(Collectors.toSet());
    return organizationWorkspaces.stream()
        .filter(workspace -> workspaceIds.contains(workspace.getId()))
        .toList();
  }

  private WorkspaceMembership ensureWorkspaceMembership(UUID workspaceId, UUID userId) {
    WorkspaceMembership membership =
        workspaceMemberships
            .findByWorkspaceIdAndUserId(workspaceId, userId)
            .orElseGet(
                () ->
                    WorkspaceMembership.builder().workspaceId(workspaceId).userId(userId).build());
    membership.setStatus(MembershipStatus.ACTIVE);
    return workspaceMemberships.save(membership);
  }

  private OrganizationMembership activateOrganizationMembership(
      UUID organizationId, User target, OrganizationRole role) {
    var existingMembership =
        organizationMemberships.findByOrganizationIdAndUserId(organizationId, target.getId());
    if (existingMembership.isPresent()
        && existingMembership.get().getStatus() == MembershipStatus.ACTIVE) {
      throw new ConflictException("Account is already an organization member");
    }
    OrganizationMembership membership =
        existingMembership.orElseGet(
            () ->
                OrganizationMembership.builder()
                    .organizationId(organizationId)
                    .userId(target.getId())
                    .role(role)
                    .status(MembershipStatus.ACTIVE)
                    .build());
    membership.setRole(role);
    membership.setStatus(MembershipStatus.ACTIVE);
    return organizationMemberships.save(membership);
  }

  private void validateAssignedRole(UUID actorId, UUID organizationId, OrganizationRole role) {
    validateInvitedRole(role);
    OrganizationMembership actor = findActiveMembership(organizationId, actorId);
    if (actor.getRole() != OrganizationRole.ORGANIZATION_OWNER
        && role != OrganizationRole.ORGANIZATION_MEMBER) {
      throw new ForbiddenException("Only the organization owner can assign elevated roles");
    }
  }

  private void validateInvitedRole(OrganizationRole role) {
    if (role == OrganizationRole.ORGANIZATION_OWNER) {
      throw new BadRequestException("Organization owner role cannot be invited or assigned");
    }
  }

  private boolean hasActiveMembership(UUID organizationId, String email) {
    return users
        .findByEmailIgnoreCase(email)
        .flatMap(
            user ->
                organizationMemberships.findByOrganizationIdAndUserId(organizationId, user.getId()))
        .map(membership -> membership.getStatus() == MembershipStatus.ACTIVE)
        .orElse(false);
  }

  private MembershipInvitation expireIfNecessary(MembershipInvitation invitation, Instant now) {
    if (invitation.getStatus() == InvitationStatus.PENDING
        && !invitation.getExpiresAt().isAfter(now)) {
      invitation.setStatus(InvitationStatus.EXPIRED);
    }
    return invitation;
  }

  private String generateToken() {
    byte[] bytes = new byte[32];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private String hashToken(String token) {
    try {
      return java.util.HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is unavailable", exception);
    }
  }

  private String normalizeEmail(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }

  private OrganizationResponse toResponse(Organization organization, UUID userId) {
    return new OrganizationResponse(
        organization.getId(),
        organization.getName(),
        organization.getJurisdictionProfile(),
        visibleWorkspaces(organization, userId).stream().map(this::toResponse).toList());
  }

  private WorkspaceResponse toResponse(Workspace workspace) {
    return new WorkspaceResponse(
        workspace.getId(),
        workspace.getOrganizationId(),
        workspace.getName(),
        workspace.isDefaultWorkspace());
  }

  private MembershipResponse toMembershipResponse(OrganizationMembership membership, User target) {
    return new MembershipResponse(
        membership.getId(),
        membership.getOrganizationId(),
        null,
        target.getId(),
        target.getEmail(),
        membership.getStatus(),
        membership.getRole());
  }

  private MembershipResponse toMembershipResponse(
      WorkspaceMembership membership, User target, UUID organizationId, OrganizationRole role) {
    return new MembershipResponse(
        membership.getId(),
        organizationId,
        membership.getWorkspaceId(),
        target.getId(),
        target.getEmail(),
        membership.getStatus(),
        role);
  }

  private InvitationResponse toInvitationResponse(MembershipInvitation invitation, String token) {
    return new InvitationResponse(
        invitation.getId(),
        invitation.getOrganizationId(),
        invitation.getWorkspaceId(),
        invitation.getEmail(),
        invitation.getRole(),
        invitation.getStatus(),
        invitation.getExpiresAt(),
        token);
  }
}
