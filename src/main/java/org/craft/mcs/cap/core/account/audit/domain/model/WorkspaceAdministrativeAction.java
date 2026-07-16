package org.craft.mcs.cap.core.account.audit.domain.model;

/** Administrative action recorded in the immutable workspace audit trail. */
public enum WorkspaceAdministrativeAction {
  /** A user created a workspace and became its first owner. */
  WORKSPACE_CREATED,

  /** An authorized member changed workspace metadata. */
  WORKSPACE_UPDATED,

  /** An owner archived the workspace. */
  WORKSPACE_ARCHIVED,

  /** An authorized member changed workspace product settings. */
  SETTINGS_UPDATED,

  /** An authorized member created a workspace invitation. */
  INVITATION_CREATED,

  /** The intended recipient accepted an invitation. */
  INVITATION_ACCEPTED,

  /** The intended recipient rejected an invitation. */
  INVITATION_REJECTED,

  /** An authorized member revoked a pending invitation. */
  INVITATION_REVOKED,

  /** An authorized member renewed an invitation validity period. */
  INVITATION_RESENT,

  /** An authorized member changed a membership role. */
  MEMBERSHIP_ROLE_CHANGED,

  /** An authorized member suspended a membership. */
  MEMBERSHIP_SUSPENDED,

  /** An authorized member reactivated a suspended membership. */
  MEMBERSHIP_REACTIVATED,

  /** An authorized member removed another membership. */
  MEMBERSHIP_REMOVED,

  /** A member voluntarily left the workspace. */
  MEMBERSHIP_LEFT,

  /** An owner transferred ownership to another active member. */
  OWNERSHIP_TRANSFERRED
}
