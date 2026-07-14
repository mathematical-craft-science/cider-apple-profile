package org.craft.mcs.cap.core.account.shared.domain.value;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/** Absolute HTTP(S) URL used by account-domain profile fields. */
@ValueObject
public record HttpUrl(@NonNull URI value) {

  private static final int MAX_LENGTH = 1_000;

  public HttpUrl {
    requireNonNull(value, "HTTP URL cannot be null");
    final var externalForm = value.toString();
    if (externalForm.isBlank()) {
      throw new DomainRuleViolationException("HTTP URL cannot be blank");
    }
    if (externalForm.length() > MAX_LENGTH) {
      throw new DomainRuleViolationException("HTTP URL cannot exceed 1000 characters");
    }
    final var scheme = value.getScheme();
    final var normalizedScheme = scheme == null ? null : scheme.toLowerCase(Locale.ROOT);
    if (!value.isAbsolute()
        || !("http".equals(normalizedScheme) || "https".equals(normalizedScheme))
        || value.getHost() == null) {
      throw new DomainRuleViolationException("HTTP URL must be absolute and use HTTP or HTTPS");
    }
  }

  public static @NonNull HttpUrl of(@NonNull URI value) {
    return new HttpUrl(value);
  }

  public static @NonNull HttpUrl of(@NonNull String value) {
    requireNonNull(value, "HTTP URL cannot be null");
    try {
      return of(new URI(value.strip()));
    } catch (URISyntaxException _) {
      throw new DomainRuleViolationException("HTTP URL has invalid format");
    }
  }

  public static @Nullable HttpUrl fromNullable(@Nullable String value) {
    return value == null || value.isBlank() ? null : of(value);
  }
}
