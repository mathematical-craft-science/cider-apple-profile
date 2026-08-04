package org.craft.mcs.cap.core.account.user.domain.model;

/** Lifecycle status of a CAP account user. */
public enum AccountUserStatus {
  /** The CAP account can participate in active workspace memberships. */
  ACTIVE,

  /** CAP has disabled product access independently of the external identity provider. */
  DISABLED,

  /** An anonymized tombstone retained so historical actor references remain resolvable. */
  DELETED
}
