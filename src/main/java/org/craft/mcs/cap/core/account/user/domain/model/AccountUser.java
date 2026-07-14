package org.craft.mcs.cap.core.account.user.domain.model;

import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.shared.domain.value.HttpUrl;
import org.craft.mcs.cap.core.account.user.domain.model.value.ExternalUserSubject;
import org.craft.mcs.cap.core.account.shared.domain.value.IdentityProviderCode;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * CAP account state linked to an external authenticated subject.
 *
 * <p>The aggregate stores the provider identity link and email alongside the CAP-owned display
 * profile. Passwords, MFA, sessions, and provider-specific security settings remain owned by the
 * external identity provider.
 */
@AggregateRoot
public record AccountUser(
    @NonNull AccountUserId userId,
    @NonNull IdentityProviderCode identityProvider,
    @NonNull ExternalUserSubject externalSubject,
    @NonNull EmailAddress email,
    @NonNull DisplayName displayName,
    @Nullable HttpUrl avatarUrl,
    @NonNull AccountUserStatus status
) {

  public AccountUser {
    requireNonNull(userId, "account user id cannot be null");
    requireNonNull(identityProvider, "identity provider cannot be null");
    requireNonNull(externalSubject, "external user subject cannot be null");
    requireNonNull(email, "account email cannot be null");
    requireNonNull(displayName, "account display name cannot be null");
    requireNonNull(status, "account user status cannot be null");
  }

  /**
   * Creates an active CAP account for an identity authenticated by an external provider.
   *
   * <p>The generated {@link AccountUserId} becomes the stable CAP identity. Provider code and
   * external subject form the external identity link, while email, display name, and avatar are
   * initialized from the first authenticated identity snapshot.
   */
  public static @NonNull AccountUser create(
      @NonNull IdentityProviderCode identityProvider,
      @NonNull ExternalUserSubject externalSubject,
      @NonNull EmailAddress email,
      @NonNull DisplayName displayName,
      @Nullable HttpUrl avatarUrl
  ) {
    return new AccountUser(
        AccountUserId.newId(),
        identityProvider,
        externalSubject,
        email,
        displayName,
        avatarUrl,
        AccountUserStatus.ACTIVE
    );
  }

  /**
   * Replaces the CAP-owned display profile without changing external identity or account status.
   *
   * <p>This operation represents an explicit user profile update inside CAP. It does not change
   * provider-owned email, provider linkage, or whether the account is allowed to use the product.
   */
  public @NonNull AccountUser updateProfile(
      @NonNull DisplayName displayName,
      @Nullable HttpUrl avatarUrl
  ) {
    return new AccountUser(
        userId,
        identityProvider,
        externalSubject,
        email,
        displayName,
        avatarUrl,
        status
    );
  }

  /**
   * Refreshes provider-owned identity data from the latest authenticated identity snapshot.
   *
   * <p>The email remains synchronized with the identity provider. CAP-owned display name
   * and avatar are preserved because provider profile values are only initial defaults supplied
   * when the account is first created.
   */
  public @NonNull AccountUser synchronizeProviderEmail(@NonNull EmailAddress email) {
    return new AccountUser(
        userId,
        identityProvider,
        externalSubject,
        email,
        displayName,
        avatarUrl,
        status
    );
  }

  /**
   * Returns whether CAP permits this account to participate in product workspaces.
   *
   * <p>An active account still requires an active workspace membership with sufficient permissions
   * for a concrete workspace operation.
   */
  public boolean isActive() {
    return status == AccountUserStatus.ACTIVE;
  }

}
