package org.craft.mcs.cap.core.account.domain.model;

import java.util.HashSet;
import java.util.Set;
import org.jspecify.annotations.NonNull;

import static org.craft.mcs.cap.core.account.domain.model.WorkspacePermission.CATALOG_CONTRIBUTE;
import static org.craft.mcs.cap.core.account.domain.model.WorkspacePermission.CATALOG_CURATE;
import static org.craft.mcs.cap.core.account.domain.model.WorkspacePermission.FEEDSTOCK_READ;
import static org.craft.mcs.cap.core.account.domain.model.WorkspacePermission.FEEDSTOCK_WRITE;
import static org.craft.mcs.cap.core.account.domain.model.WorkspacePermission.LAB_CONTRIBUTE;
import static org.craft.mcs.cap.core.account.domain.model.WorkspacePermission.PRODUCTION_READ;
import static org.craft.mcs.cap.core.account.domain.model.WorkspacePermission.PRODUCTION_WRITE;
import static org.craft.mcs.cap.core.account.domain.model.WorkspacePermission.WORKSPACE_MEMBERS_READ;
import static org.craft.mcs.cap.core.account.domain.model.WorkspacePermission.WORKSPACE_READ;
import static org.craft.mcs.cap.core.account.domain.model.WorkspacePermission.WORKSPACE_SETTINGS_READ;


/** CAP workspace role. External-provider roles are mapped to this model only at the ACL boundary. */
public enum AccountRole {
  /** Owns the workspace and has every CAP permission within it. */
  OWNER,

  /** Administers the workspace and currently has the same product permissions as the owner. */
  ADMIN,

  /** Plans and supervises feedstock and production processes. */
  TECHNOLOGIST,

  /** Operates feedstock and production processes as a cidermaker. */
  CIDERMAKER,

  /** Contributes variety catalog data without production workspace access. */
  CATALOG_CONTRIBUTOR,

  /** Contributes laboratory evidence and may read source-material context. */
  LAB_CONTRIBUTOR,

  /** Reviews and curates contributed catalog data. */
  CURATOR,

  /** Reads workspace feedstock and production data without modifying it. */
  VIEWER;

  private static final @NonNull Set<@NonNull WorkspacePermission> WORKSPACE_BASE_PERMISSIONS = Set.of(
      WORKSPACE_READ,
      WORKSPACE_SETTINGS_READ,
      WORKSPACE_MEMBERS_READ
  );

  private static final @NonNull Set<@NonNull WorkspacePermission> BUSINESS_READ_PERMISSIONS = with(
      WORKSPACE_BASE_PERMISSIONS,
      FEEDSTOCK_READ,
      PRODUCTION_READ
  );

  private static final @NonNull Set<@NonNull WorkspacePermission> PRODUCTION_PERMISSIONS = Set.of(
      WORKSPACE_READ,
      WORKSPACE_SETTINGS_READ,
      WORKSPACE_MEMBERS_READ,
      FEEDSTOCK_READ,
      FEEDSTOCK_WRITE,
      PRODUCTION_READ,
      PRODUCTION_WRITE
  );

  /** Returns stable permissions granted by this role within its workspace membership. */
  public @NonNull Set<@NonNull WorkspacePermission> workspacePermissions() {
    return switch (this) {
      case OWNER, ADMIN -> Set.of(WorkspacePermission.values());
      case TECHNOLOGIST, CIDERMAKER -> PRODUCTION_PERMISSIONS;
      case CATALOG_CONTRIBUTOR -> with(WORKSPACE_BASE_PERMISSIONS, CATALOG_CONTRIBUTE);
      case LAB_CONTRIBUTOR -> with(WORKSPACE_BASE_PERMISSIONS, FEEDSTOCK_READ, LAB_CONTRIBUTE);
      case CURATOR -> with(WORKSPACE_BASE_PERMISSIONS, CATALOG_CONTRIBUTE, CATALOG_CURATE);
      case VIEWER -> BUSINESS_READ_PERMISSIONS;
    };
  }

  private static @NonNull Set<@NonNull WorkspacePermission> with(
      @NonNull Set<@NonNull WorkspacePermission> base,
      @NonNull WorkspacePermission... additions
  ) {
    final var permissions = new HashSet<>(base);
    permissions.addAll(Set.of(additions));
    return Set.copyOf(permissions);
  }
}
