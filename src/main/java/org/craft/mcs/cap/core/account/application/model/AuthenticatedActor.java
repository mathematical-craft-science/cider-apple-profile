package org.craft.mcs.cap.core.account.application.model;

import org.craft.mcs.cap.core.account.domain.model.value.DisplayName;
import org.craft.mcs.cap.core.account.domain.model.value.EmailAddress;
import org.craft.mcs.cap.core.account.domain.model.value.ExternalOrganizationSubject;
import org.craft.mcs.cap.core.account.domain.model.value.ExternalUserSubject;
import org.craft.mcs.cap.core.account.domain.model.value.HttpUrl;
import org.craft.mcs.cap.core.account.domain.model.value.IdentityProviderCode;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Provider-neutral authenticated identity visible to CAP application services. */
public record AuthenticatedActor(
    @NonNull IdentityProviderCode identityProvider,
    @NonNull ExternalUserSubject externalSubject,
    @NonNull EmailAddress email,
    @NonNull DisplayName displayName,
    @Nullable HttpUrl avatarUrl,
    @Nullable ExternalOrganizationSubject externalOrganizationSubject
) {

  public AuthenticatedActor {
    requireNonNull(identityProvider, "identity provider cannot be null");
    requireNonNull(externalSubject, "external user subject cannot be null");
    requireNonNull(email, "authenticated actor email cannot be null");
    requireNonNull(displayName, "authenticated actor display name cannot be null");
  }
}
