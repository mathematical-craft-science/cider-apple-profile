package org.craft.mcs.cap.shared.kernel.identity;


/** Type of CAP-owned identity that can perform an auditable action. */
public enum ActorType {

  /** Human user represented by an internal CAP account. */
  ACCOUNT_USER,

  /** Non-human identity used by an integration or automated process. */
  MACHINE_IDENTITY
}
