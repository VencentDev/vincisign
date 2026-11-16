package com.vencentdev.backend.modules.organization.service;

import com.vencentdev.backend.modules.organization.dto.RolePermissionResponse;
import com.vencentdev.backend.modules.organization.enums.OrganizationPermission;
import com.vencentdev.backend.modules.organization.enums.OrganizationRole;
import com.vencentdev.backend.modules.organization.enums.PlatformPermission;
import com.vencentdev.backend.modules.organization.enums.PlatformRole;
import com.vencentdev.backend.modules.organization.enums.ProjectPermission;
import com.vencentdev.backend.modules.organization.enums.ProjectRole;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class RolePermissionCatalog {

  private static final Map<OrganizationRole, Set<OrganizationPermission>> ORGANIZATION_PERMISSIONS =
      organizationPermissions();
  private static final Map<ProjectRole, Set<ProjectPermission>> PROJECT_PERMISSIONS =
      projectPermissions();
  private static final Map<PlatformRole, Set<PlatformPermission>> PLATFORM_PERMISSIONS =
      platformPermissions();

  private RolePermissionCatalog() {}

  public static Set<OrganizationPermission> organizationPermissions(OrganizationRole role) {
    return ORGANIZATION_PERMISSIONS.get(role);
  }

  public static List<RolePermissionResponse> all() {
    List<RolePermissionResponse> platform =
        PLATFORM_PERMISSIONS.entrySet().stream()
            .map(
                entry ->
                    new RolePermissionResponse(
                        "PLATFORM", entry.getKey().name(), names(entry.getValue())))
            .toList();
    List<RolePermissionResponse> organization =
        ORGANIZATION_PERMISSIONS.entrySet().stream()
            .map(
                entry ->
                    new RolePermissionResponse(
                        "ORGANIZATION", entry.getKey().name(), names(entry.getValue())))
            .toList();
    List<RolePermissionResponse> project =
        PROJECT_PERMISSIONS.entrySet().stream()
            .map(
                entry ->
                    new RolePermissionResponse(
                        "PROJECT", entry.getKey().name(), names(entry.getValue())))
            .toList();
    return java.util.stream.Stream.of(platform, organization, project)
        .flatMap(List::stream)
        .toList();
  }

  private static Map<OrganizationRole, Set<OrganizationPermission>> organizationPermissions() {
    EnumMap<OrganizationRole, Set<OrganizationPermission>> permissions =
        new EnumMap<>(OrganizationRole.class);
    permissions.put(
        OrganizationRole.ORGANIZATION_OWNER, EnumSet.allOf(OrganizationPermission.class));
    permissions.put(
        OrganizationRole.ORGANIZATION_ADMINISTRATOR,
        EnumSet.of(
            OrganizationPermission.ORGANIZATION_VIEW,
            OrganizationPermission.ORGANIZATION_MANAGE,
            OrganizationPermission.WORKSPACE_VIEW,
            OrganizationPermission.WORKSPACE_MANAGE,
            OrganizationPermission.MEMBERSHIP_VIEW,
            OrganizationPermission.MEMBERSHIP_MANAGE,
            OrganizationPermission.PROJECT_CREATE,
            OrganizationPermission.PROJECT_VIEW,
            OrganizationPermission.PROJECT_SEND,
            OrganizationPermission.AUDIT_VIEW,
            OrganizationPermission.API_CLIENT_MANAGE));
    permissions.put(
        OrganizationRole.ORGANIZATION_MEMBER,
        EnumSet.of(
            OrganizationPermission.ORGANIZATION_VIEW,
            OrganizationPermission.WORKSPACE_VIEW,
            OrganizationPermission.PROJECT_CREATE,
            OrganizationPermission.PROJECT_VIEW));
    permissions.put(
        OrganizationRole.AUDITOR,
        EnumSet.of(
            OrganizationPermission.ORGANIZATION_VIEW,
            OrganizationPermission.WORKSPACE_VIEW,
            OrganizationPermission.PROJECT_VIEW,
            OrganizationPermission.AUDIT_VIEW));
    permissions.put(
        OrganizationRole.API_OPERATOR,
        EnumSet.of(
            OrganizationPermission.ORGANIZATION_VIEW,
            OrganizationPermission.WORKSPACE_VIEW,
            OrganizationPermission.PROJECT_CREATE,
            OrganizationPermission.PROJECT_VIEW,
            OrganizationPermission.PROJECT_SEND,
            OrganizationPermission.API_CLIENT_MANAGE));
    return Map.copyOf(permissions);
  }

  private static Map<ProjectRole, Set<ProjectPermission>> projectPermissions() {
    EnumMap<ProjectRole, Set<ProjectPermission>> permissions = new EnumMap<>(ProjectRole.class);
    permissions.put(
        ProjectRole.SENDER,
        EnumSet.of(
            ProjectPermission.PROJECT_VIEW,
            ProjectPermission.PROJECT_CREATE,
            ProjectPermission.PROJECT_PREPARE,
            ProjectPermission.PROJECT_SEND));
    permissions.put(
        ProjectRole.SIGNER,
        EnumSet.of(ProjectPermission.PROJECT_VIEW, ProjectPermission.PROJECT_SIGN));
    permissions.put(
        ProjectRole.WITNESS,
        EnumSet.of(ProjectPermission.PROJECT_VIEW, ProjectPermission.PROJECT_WITNESS));
    permissions.put(
        ProjectRole.APPROVER,
        EnumSet.of(ProjectPermission.PROJECT_VIEW, ProjectPermission.PROJECT_APPROVE));
    permissions.put(ProjectRole.VIEWER, EnumSet.of(ProjectPermission.PROJECT_VIEW));
    permissions.put(
        ProjectRole.NOTARY,
        EnumSet.of(ProjectPermission.PROJECT_VIEW, ProjectPermission.PROJECT_NOTARIZE));
    permissions.put(
        ProjectRole.NOTARY_SUPERVISOR,
        EnumSet.of(
            ProjectPermission.PROJECT_VIEW,
            ProjectPermission.PROJECT_NOTARIZE,
            ProjectPermission.NOTARY_SUPERVISE));
    permissions.put(
        ProjectRole.GUEST_RECIPIENT,
        EnumSet.of(ProjectPermission.PROJECT_VIEW, ProjectPermission.PROJECT_SIGN));
    return Map.copyOf(permissions);
  }

  private static Map<PlatformRole, Set<PlatformPermission>> platformPermissions() {
    EnumMap<PlatformRole, Set<PlatformPermission>> permissions = new EnumMap<>(PlatformRole.class);
    permissions.put(PlatformRole.PLATFORM_OWNER, EnumSet.allOf(PlatformPermission.class));
    permissions.put(
        PlatformRole.COMPLIANCE_ADMINISTRATOR,
        EnumSet.of(
            PlatformPermission.ORGANIZATION_INVESTIGATE,
            PlatformPermission.COMPLIANCE_REVIEW,
            PlatformPermission.AUDIT_READ));
    return Map.copyOf(permissions);
  }

  private static List<String> names(Set<? extends Enum<?>> values) {
    return values.stream().map(Enum::name).sorted().toList();
  }
}
