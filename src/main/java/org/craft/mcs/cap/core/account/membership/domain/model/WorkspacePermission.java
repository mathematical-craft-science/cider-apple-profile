package org.craft.mcs.cap.core.account.membership.domain.model;

/** Product permission granted by a role within one CAP workspace. */
public enum WorkspacePermission {
  /** Read workspace metadata. */
  WORKSPACE_READ,

  /** Update workspace name or business type. */
  WORKSPACE_UPDATE,

  /** Archive a workspace while preserving its history. */
  WORKSPACE_ARCHIVE,

  /** Transfer workspace ownership to another active member. */
  WORKSPACE_OWNERSHIP_TRANSFER,

  /** Read workspace product settings. */
  WORKSPACE_SETTINGS_READ,

  /** Update workspace product settings. */
  WORKSPACE_SETTINGS_UPDATE,

  /** Read workspace membership information. */
  WORKSPACE_MEMBERS_READ,

  /** Invite, suspend, or change roles of workspace members. */
  WORKSPACE_MEMBERS_MANAGE,

  /** Read the immutable workspace administration audit trail. */
  WORKSPACE_AUDIT_READ,

  /** Read feedstock batches, measurements, and their assessments. */
  FEEDSTOCK_READ,

  /** Create or change feedstock batches and measurements. */
  FEEDSTOCK_WRITE,

  /** Read production batches and traceability views. */
  PRODUCTION_READ,

  /** Create or change production batches and operations. */
  PRODUCTION_WRITE,

  /** Add catalog and regional-profile contributions. */
  CATALOG_CONTRIBUTE,

  /** Add laboratory measurements and provenance evidence. */
  LAB_CONTRIBUTE,

  /** Review and publish contributed catalog data. */
  CATALOG_CURATE
}
