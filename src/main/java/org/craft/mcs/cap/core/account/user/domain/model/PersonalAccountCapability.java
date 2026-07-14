package org.craft.mcs.cap.core.account.user.domain.model;

import java.util.Set;
import org.jspecify.annotations.NonNull;

/** Capability an active CAP user has over their own provider-neutral account data. */
public enum PersonalAccountCapability {
  /** Read the current CAP-owned profile. */
  PROFILE_READ,

  /** Update CAP-owned display profile fields. */
  PROFILE_UPDATE,

  /** Read personal locale and time-zone preferences. */
  PREFERENCES_READ,

  /** Update personal locale and time-zone preferences. */
  PREFERENCES_UPDATE,

  /** Create an additional CAP workspace owned by the current account. */
  WORKSPACE_CREATE,

  /** Read the current account's contributor profile. */
  CONTRIBUTOR_PROFILE_READ,

  /** Create or update the current account's contributor profile. */
  CONTRIBUTOR_PROFILE_UPDATE,

  /** Delete the current account's contributor profile. */
  CONTRIBUTOR_PROFILE_DELETE;

  private static final @NonNull Set<@NonNull PersonalAccountCapability> ACTIVE_ACCOUNT_CAPABILITIES =
      Set.of(values());

  /** Returns capabilities available to every authenticated active CAP account. */
  public static @NonNull Set<@NonNull PersonalAccountCapability> forActiveAccount() {
    return ACTIVE_ACCOUNT_CAPABILITIES;
  }
}
