package org.craft.mcs.cap.core.account.application.port.in;

import java.time.ZoneId;
import java.util.Locale;
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
}
