package org.craft.mcs.cap.core.account.user.domain.model;

import java.time.ZoneId;
import java.util.Locale;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/**
 * User-specific presentation preferences that are independent of the identity provider.
 */
@AggregateRoot
public record UserPreferences(
    @NonNull AccountUserId userId,
    @NonNull Locale locale,
    @NonNull ZoneId timeZone
) {

  public UserPreferences {
    requireNonNull(userId, "account user id cannot be null");
    requireNonNull(locale, "user locale cannot be null");
    requireNonNull(timeZone, "user time zone cannot be null");
  }

  public static @NonNull UserPreferences defaults(@NonNull AccountUserId userId) {
    return new UserPreferences(userId, Locale.ENGLISH, ZoneId.of("UTC"));
  }
}
