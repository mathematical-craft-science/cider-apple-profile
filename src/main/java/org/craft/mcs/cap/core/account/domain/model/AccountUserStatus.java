package org.craft.mcs.cap.core.account.domain.model;


/** Lifecycle status of a CAP account user. */
public enum AccountUserStatus {
  /** The CAP account can participate in active workspace memberships. */
  ACTIVE,

  /** CAP has disabled product access independently of the external identity provider. */
  DISABLED
}
