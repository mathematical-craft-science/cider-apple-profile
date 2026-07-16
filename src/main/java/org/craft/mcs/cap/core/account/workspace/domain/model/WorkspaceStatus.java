package org.craft.mcs.cap.core.account.workspace.domain.model;

/** Lifecycle state controlling whether a workspace can be used as an active product scope. */
public enum WorkspaceStatus {
  /** The workspace can be selected and its memberships can grant product permissions. */
  ACTIVE,

  /** The workspace is retained for history but cannot be selected for new product operations. */
  ARCHIVED
}
