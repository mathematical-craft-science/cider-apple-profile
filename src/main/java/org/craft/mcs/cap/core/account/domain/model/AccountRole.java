package org.craft.mcs.cap.core.account.domain.model;

import java.util.HashSet;
import java.util.Set;
import org.jspecify.annotations.NonNull;

import static org.craft.mcs.cap.core.account.domain.model.AccountPermission.ACCOUNT_READ;
import static org.craft.mcs.cap.core.account.domain.model.AccountPermission.ACCOUNT_UPDATE;
import static org.craft.mcs.cap.core.account.domain.model.AccountPermission.CATALOG_CONTRIBUTE;
import static org.craft.mcs.cap.core.account.domain.model.AccountPermission.CATALOG_CURATE;
import static org.craft.mcs.cap.core.account.domain.model.AccountPermission.FEEDSTOCK_READ;
import static org.craft.mcs.cap.core.account.domain.model.AccountPermission.FEEDSTOCK_WRITE;
import static org.craft.mcs.cap.core.account.domain.model.AccountPermission.LAB_CONTRIBUTE;
import static org.craft.mcs.cap.core.account.domain.model.AccountPermission.PREFERENCES_READ;
import static org.craft.mcs.cap.core.account.domain.model.AccountPermission.PREFERENCES_UPDATE;
import static org.craft.mcs.cap.core.account.domain.model.AccountPermission.PRODUCTION_READ;
import static org.craft.mcs.cap.core.account.domain.model.AccountPermission.PRODUCTION_WRITE;
import static org.craft.mcs.cap.core.account.domain.model.AccountPermission.WORKSPACE_MEMBERS_READ;
import static org.craft.mcs.cap.core.account.domain.model.AccountPermission.WORKSPACE_READ;


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

  private static final @NonNull Set<@NonNull AccountPermission> ACCOUNT_PERMISSIONS = Set.of(
      ACCOUNT_READ,
      PREFERENCES_READ,
      WORKSPACE_READ,
      WORKSPACE_MEMBERS_READ
  );

  private static final @NonNull Set<@NonNull AccountPermission> BUSINESS_READ_PERMISSIONS = with(
      ACCOUNT_PERMISSIONS,
      FEEDSTOCK_READ,
      PRODUCTION_READ
  );

  private static final @NonNull Set<@NonNull AccountPermission> PRODUCTION_PERMISSIONS = Set.of(
      ACCOUNT_READ,
      ACCOUNT_UPDATE,
      PREFERENCES_READ,
      PREFERENCES_UPDATE,
      WORKSPACE_READ,
      WORKSPACE_MEMBERS_READ,
      FEEDSTOCK_READ,
      FEEDSTOCK_WRITE,
      PRODUCTION_READ,
      PRODUCTION_WRITE
  );

  /** Returns stable CAP permissions granted by this membership role. */
  public @NonNull Set<@NonNull AccountPermission> permissions() {
    return switch (this) {
      case OWNER, ADMIN -> Set.of(AccountPermission.values());
      case TECHNOLOGIST, CIDERMAKER -> PRODUCTION_PERMISSIONS;
      case CATALOG_CONTRIBUTOR -> with(ACCOUNT_PERMISSIONS, CATALOG_CONTRIBUTE);
      case LAB_CONTRIBUTOR -> with(ACCOUNT_PERMISSIONS, FEEDSTOCK_READ, LAB_CONTRIBUTE);
      case CURATOR -> with(ACCOUNT_PERMISSIONS, CATALOG_CONTRIBUTE, CATALOG_CURATE);
      case VIEWER -> BUSINESS_READ_PERMISSIONS;
    };
  }

  private static @NonNull Set<@NonNull AccountPermission> with(
      @NonNull Set<@NonNull AccountPermission> base,
      @NonNull AccountPermission... additions
  ) {
    final var permissions = new HashSet<>(base);
    permissions.addAll(Set.of(additions));
    return Set.copyOf(permissions);
  }
}
