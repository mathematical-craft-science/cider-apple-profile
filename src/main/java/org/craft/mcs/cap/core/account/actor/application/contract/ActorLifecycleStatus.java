package org.craft.mcs.cap.core.account.actor.application.contract;


/** Lifecycle status exposed by the provider-neutral actor directory. */
public enum ActorLifecycleStatus {

  /** Actor may currently authenticate or perform CAP actions. */
  ACTIVE,

  /** Actor is retained for display and audit but cannot currently perform actions. */
  INACTIVE,

  /** Actor is an anonymized tombstone retained to preserve historical attribution. */
  DELETED
}
