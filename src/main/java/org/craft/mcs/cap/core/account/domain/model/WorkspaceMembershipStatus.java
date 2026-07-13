package org.craft.mcs.cap.core.account.domain.model;


/** Lifecycle status of a CAP workspace membership. */
public enum WorkspaceMembershipStatus {
  /** The user can exercise permissions granted by the membership role. */
  ACTIVE,

  /** The membership has been invited but has not yet become active. */
  INVITED,

  /** Workspace administrators have temporarily disabled this membership. */
  SUSPENDED
}
