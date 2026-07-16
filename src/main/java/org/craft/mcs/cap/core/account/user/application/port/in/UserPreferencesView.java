package org.craft.mcs.cap.core.account.user.application.port.in;

import java.time.ZoneId;
import java.util.Locale;
import org.craft.mcs.cap.core.account.user.domain.model.UserPreferences;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Read model of the current user's CAP presentation preferences. */
public record UserPreferencesView(
    @NonNull Locale locale,
    @NonNull ZoneId timeZone
) {

  public UserPreferencesView {
    requireNonNull(locale, "user locale cannot be null");
    requireNonNull(timeZone, "user time zone cannot be null");
  }

  /** Builds the application read model from persisted personal preferences. */
  public static @NonNull UserPreferencesView from(@NonNull UserPreferences preferences) {
    requireNonNull(preferences, "user preferences cannot be null");
    return new UserPreferencesView(preferences.locale(), preferences.timeZone());
  }
}
