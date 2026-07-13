package org.craft.mcs.cap.core.account.domain.model;


/** Product permissions derived from CAP workspace membership roles. */
public enum AccountPermission {
  /** Read the current CAP-owned account profile. */
  ACCOUNT_READ,

  /** Update CAP-owned account profile fields. */
  ACCOUNT_UPDATE,

  /** Read personal CAP preferences. */
  PREFERENCES_READ,

  /** Update personal CAP preferences. */
  PREFERENCES_UPDATE,

  /** Read workspace metadata. */
  WORKSPACE_READ,

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
