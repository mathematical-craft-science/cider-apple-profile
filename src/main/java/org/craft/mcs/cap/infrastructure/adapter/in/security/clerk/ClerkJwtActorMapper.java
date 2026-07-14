package org.craft.mcs.cap.infrastructure.adapter.in.security.clerk;

import java.util.function.Supplier;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.shared.domain.value.HttpUrl;
import org.craft.mcs.cap.core.account.user.application.model.AuthenticatedActor;
import org.craft.mcs.cap.core.account.user.domain.model.value.ExternalUserSubject;
import org.craft.mcs.cap.core.account.shared.domain.value.IdentityProviderCode;
import org.craft.mcs.cap.core.account.workspace.domain.model.value.ExternalOrganizationSubject;
import org.craft.mcs.cap.shared.kernel.DomainException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * Anti-corruption mapper from Clerk JWT claims to CAP's provider-neutral authenticated actor.
 *
 * <p>CAP requires a custom primary-email claim because Clerk session tokens do not include profile
 * data by default. Camel-case claim names follow Clerk's documented customization example; legacy
 * aliases remain accepted for existing environments.
 */
@Component
public final class ClerkJwtActorMapper {

  public @NonNull AuthenticatedActor toActor(@NonNull Jwt jwt) {
    requireNonNull(jwt, "JWT cannot be null");
    final var subject = required(jwt.getSubject(), "Clerk JWT subject is required");
    final var email = required(firstClaim(jwt, "primaryEmail", "email", "primary_email_address"),
        "Clerk JWT email claim is required");
    final var externalSubject = requiredIdentityValue(() -> ExternalUserSubject.of(subject));
    final var emailAddress = requiredIdentityValue(() -> EmailAddress.of(email));
    final var displayName = firstClaim(jwt, "fullName", "name", "full_name", "username");
    final var actorDisplayName = requiredIdentityValue(() -> DisplayName.of(
        displayName == null ? emailAddress.localPart() : displayName
    ));
    final var organizationSubject = firstClaim(jwt, "org_id", "organization_id");

    return new AuthenticatedActor(
        IdentityProviderCode.CLERK,
        externalSubject,
        emailAddress,
        actorDisplayName,
        identityValue(() -> HttpUrl.fromNullable(firstClaim(jwt, "imageUrl", "image_url", "picture"))),
        identityValue(() -> organizationSubject == null
            ? null
            : ExternalOrganizationSubject.of(organizationSubject))
    );
  }

  private static <T extends @NonNull Object> T requiredIdentityValue(
      @NonNull Supplier<T> supplier
  ) {
    return requireNonNull(identityValue(supplier), "required Clerk JWT identity value cannot be null");
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
