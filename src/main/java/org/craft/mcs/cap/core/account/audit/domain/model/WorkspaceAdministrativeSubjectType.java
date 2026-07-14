package org.craft.mcs.cap.core.account.audit.domain.model;

/** Type of workspace-owned subject referenced by an administrative audit entry. */
public enum WorkspaceAdministrativeSubjectType {
  /** Workspace lifecycle and metadata. */
  WORKSPACE,

  /** Workspace-owned product settings. */
  WORKSPACE_SETTINGS,

  /** Pre-membership invitation. */
  INVITATION,

  /** Workspace membership and role state. */
  MEMBERSHIP
}
