package org.craft.mcs.cap.core.account.application.port.in;

import java.time.ZoneId;
import java.util.Locale;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Command for changing user-specific locale and time-zone preferences. */
public record UpdateUserPreferencesCommand(
    @NonNull Locale locale,
    @NonNull ZoneId timeZone
) {

  public UpdateUserPreferencesCommand {
    requireNonNull(locale, "user locale cannot be null");
    requireNonNull(timeZone, "user time zone cannot be null");
  }
}
