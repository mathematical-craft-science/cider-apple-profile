package org.craft.mcs.cap.infrastructure.adapter.out.requestcontext.account.oauth2;

import java.util.function.Supplier;
import org.craft.mcs.cap.shared.kernel.DomainException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;

import static java.util.Objects.requireNonNull;

/** Reads and validates identity claims without exposing provider JWT semantics to CAP application code. */
public final class JwtIdentityClaimReader {

  private final @NonNull String providerName;

  public JwtIdentityClaimReader(@NonNull String providerName) {
    this.providerName = requireNonNull(providerName, "identity provider name cannot be null");
  }

  public @Nullable String firstString(
      @NonNull Jwt jwt,
      @NonNull String... claimNames
  ) {
    requireNonNull(jwt, "JWT cannot be null");
    requireNonNull(claimNames, "JWT claim names cannot be null");
    for (final var claimName : claimNames) {
      final var value = jwt.getClaimAsString(claimName);
      if (value != null && !value.isBlank()) {
        return value.strip();
      }
    }
    return null;
  }

  public @NonNull String requiredString(
      @Nullable String value,
      @NonNull String message
  ) {
    if (value == null || value.isBlank()) {
      throw new BadCredentialsException(message);
    }
    return value.strip();
  }

  public <T extends @NonNull Object> T requiredIdentityValue(@NonNull Supplier<T> supplier) {
    return requireNonNull(identityValue(supplier), "required JWT identity value cannot be null");
  }

  public <T extends @Nullable Object> T identityValue(@NonNull Supplier<T> supplier) {
    requireNonNull(supplier, "identity value supplier cannot be null");
    try {
      return supplier.get();
    } catch (DomainException exception) {
      throw new BadCredentialsException(providerName + " JWT identity claims are invalid", exception);
    }
  }
}
