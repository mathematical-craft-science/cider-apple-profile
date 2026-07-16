package org.craft.mcs.cap.core.account.membership.domain.model;

/** Lifecycle status of a CAP workspace membership. */
public enum WorkspaceMembershipStatus {
  /** The user can exercise permissions granted by the membership role. */
  ACTIVE,

  /** Workspace administrators have temporarily disabled this membership. */
  SUSPENDED
}
