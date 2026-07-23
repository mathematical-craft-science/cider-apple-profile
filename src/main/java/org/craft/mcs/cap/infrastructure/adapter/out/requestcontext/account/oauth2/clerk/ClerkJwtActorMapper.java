package org.craft.mcs.cap.infrastructure.adapter.out.requestcontext.account.oauth2.clerk;

import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.shared.domain.value.HttpUrl;
import org.craft.mcs.cap.core.account.user.application.model.AuthenticatedActor;
import org.craft.mcs.cap.core.account.user.domain.model.value.ExternalUserSubject;
import org.craft.mcs.cap.core.account.shared.domain.value.IdentityProviderCode;
import org.craft.mcs.cap.core.account.shared.domain.value.ExternalOrganizationSubject;
import org.craft.mcs.cap.infrastructure.adapter.out.requestcontext.account.oauth2.JwtActorMapper;
import org.craft.mcs.cap.infrastructure.adapter.out.requestcontext.account.oauth2.JwtIdentityClaimReader;
import org.jspecify.annotations.NonNull;
import org.springframework.security.oauth2.jwt.Jwt;

import static java.util.Objects.requireNonNull;

/**
 * Anti-corruption mapper from Clerk JWT claims to CAP's provider-neutral authenticated actor.
 *
 * <p>CAP requires a custom primary-email claim because Clerk session tokens do not include profile
 * data by default. Camel-case claim names follow Clerk's documented customization example; legacy
 * aliases remain accepted for existing environments.
 */
public final class ClerkJwtActorMapper implements JwtActorMapper {

  private final @NonNull JwtIdentityClaimReader claims = new JwtIdentityClaimReader("Clerk");

  @Override
  public @NonNull AuthenticatedActor toActor(@NonNull Jwt jwt) {
    requireNonNull(jwt, "JWT cannot be null");
    final var subject = claims.requiredString(jwt.getSubject(), "Clerk JWT subject is required");
    final var email = claims.requiredString(
        claims.firstString(jwt, "primaryEmail", "email", "primary_email_address"),
        "Clerk JWT email claim is required");
    final var externalSubject = claims.requiredIdentityValue(() -> ExternalUserSubject.of(subject));
    final var emailAddress = claims.requiredIdentityValue(() -> EmailAddress.of(email));
    final var displayName = claims.firstString(jwt, "fullName", "name", "full_name", "username");
    final var actorDisplayName = claims.requiredIdentityValue(() -> DisplayName.of(
        displayName == null ? emailAddress.localPart() : displayName
    ));
    final var organizationSubject = claims.firstString(jwt, "org_id", "organization_id");

    return new AuthenticatedActor(
        IdentityProviderCode.CLERK,
        externalSubject,
        emailAddress,
        actorDisplayName,
        claims.identityValue(() -> HttpUrl.fromNullable(
            claims.firstString(jwt, "imageUrl", "image_url", "picture")
        )),
        claims.identityValue(() -> organizationSubject == null
            ? null
            : ExternalOrganizationSubject.of(organizationSubject))
    );
  }
}
