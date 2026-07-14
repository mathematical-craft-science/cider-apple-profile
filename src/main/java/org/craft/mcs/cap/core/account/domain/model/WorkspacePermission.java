package org.craft.mcs.cap.core.account.domain.model;


/** Product permission granted by a role within one CAP workspace. */
public enum WorkspacePermission {
  /** Read workspace metadata. */
  WORKSPACE_READ,

  /** Read workspace product settings. */
  WORKSPACE_SETTINGS_READ,

  /** Update workspace product settings. */
  WORKSPACE_SETTINGS_UPDATE,

  /** Read workspace membership information. */
  WORKSPACE_MEMBERS_READ,

  /** Invite, suspend, or change roles of workspace members. */
  WORKSPACE_MEMBERS_MANAGE,

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
