package org.craft.mcs.cap.infrastructure.adapter.in.security.clerk;

import java.util.function.Supplier;
import org.craft.mcs.cap.core.account.application.model.AuthenticatedActor;
import org.craft.mcs.cap.core.account.domain.model.value.DisplayName;
import org.craft.mcs.cap.core.account.domain.model.value.EmailAddress;
import org.craft.mcs.cap.core.account.domain.model.value.ExternalOrganizationSubject;
import org.craft.mcs.cap.core.account.domain.model.value.ExternalUserSubject;
import org.craft.mcs.cap.core.account.domain.model.value.HttpUrl;
import org.craft.mcs.cap.core.account.domain.model.value.IdentityProviderCode;
import org.craft.mcs.cap.shared.kernel.DomainException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Anti-corruption mapper from Clerk JWT claims to CAP's provider-neutral authenticated actor. */
@Component
public final class ClerkJwtActorMapper {

  public @NonNull AuthenticatedActor toActor(@NonNull Jwt jwt) {
    requireNonNull(jwt, "JWT cannot be null");
    final var subject = required(jwt.getSubject(), "Clerk JWT subject is required");
    final var email = required(firstClaim(jwt, "email", "primary_email_address"),
        "Clerk JWT email claim is required");
    final var emailAddress = identityValue(() -> EmailAddress.of(email));
    final var displayName = firstClaim(jwt, "name", "full_name", "username");
    final var organizationSubject = firstClaim(jwt, "org_id", "organization_id");

    return new AuthenticatedActor(
        IdentityProviderCode.CLERK,
        ExternalUserSubject.of(subject),
        emailAddress,
        identityValue(() -> DisplayName.of(
            displayName == null ? emailAddress.localPart() : displayName
        )),
        identityValue(() -> HttpUrl.fromNullable(firstClaim(jwt, "image_url", "picture"))),
        organizationSubject == null ? null : ExternalOrganizationSubject.of(organizationSubject)
    );
  }

  private static <T extends @Nullable Object> T identityValue(@NonNull Supplier<T> supplier) {
    try {
      return supplier.get();
    } catch (DomainException exception) {
      throw new BadCredentialsException("Clerk JWT identity claims are invalid", exception);
    }
  }

  private static @Nullable String firstClaim(
      @NonNull Jwt jwt,
      @NonNull String... claimNames
  ) {
    for (final var claimName : claimNames) {
      final var value = jwt.getClaimAsString(claimName);
      if (value != null && !value.isBlank()) {
        return value.strip();
      }
    }
    return null;
  }

  private static @NonNull String required(
      @Nullable String value,
      @NonNull String message
  ) {
    if (value == null || value.isBlank()) {
      throw new BadCredentialsException(message);
    }
    return value.strip();
  }
}
