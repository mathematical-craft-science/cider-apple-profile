package org.craft.mcs.cap.core.account.invitation.domain.model;

/** Lifecycle status of an invitation that exists before workspace membership creation. */
public enum WorkspaceInvitationStatus {
  /** The invitation can be accepted or rejected before its expiration time. */
  PENDING,

  /** The invited account accepted and received an active workspace membership. */
  ACCEPTED,

  /** The invited account explicitly rejected the invitation. */
  REJECTED,

  /** The invitation elapsed without acceptance and can be resent. */
  EXPIRED,

  /** A workspace administrator revoked the invitation before acceptance. */
  REVOKED
}
