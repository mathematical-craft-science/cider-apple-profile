package org.craft.mcs.cap.account.user.domain.model;

import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.shared.domain.value.HttpUrl;
import org.craft.mcs.cap.core.account.shared.domain.value.IdentityProviderCode;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUser;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUserLifecycleException;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUserStatus;
import org.craft.mcs.cap.core.account.user.domain.model.value.ExternalUserSubject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class AccountUserTest {

  @Test
  void retainsAnonymizedTombstoneForHistoricalActorResolution() {
    final var user = AccountUser.create(
        IdentityProviderCode.KEYCLOAK,
        ExternalUserSubject.of("provider-user-42"),
        EmailAddress.of("person@example.com"),
        DisplayName.of("Person Name"),
        HttpUrl.of("https://example.com/avatar.png")
    );

    final var deleted = user.retainDeletedTombstone();

    assertThat(deleted)
        .returns(user.userId(), AccountUser::userId)
        .returns(user.identityProvider(), AccountUser::identityProvider)
        .returns(AccountUserStatus.DELETED, AccountUser::status)
        .returns(DisplayName.of("Deleted user"), AccountUser::displayName)
        .returns(null, AccountUser::avatarUrl)
        .matches(account -> account.externalSubject().value().startsWith("deleted:"))
        .matches(account -> account.email().value().endsWith("@deleted.invalid"))
        .matches(account -> !account.isActive());
  }

  @Test
  void retainingDeletedTombstoneIsIdempotent() {
    final var deleted = AccountUser.create(
        IdentityProviderCode.CLERK,
        ExternalUserSubject.of("provider-user-43"),
        EmailAddress.of("another@example.com"),
        DisplayName.of("Another person"),
        null
    ).retainDeletedTombstone();

    assertThat(deleted.retainDeletedTombstone()).isSameAs(deleted);
  }

  @Test
  void deletedTombstoneCannotBeChanged() {
    final var deleted = accountUser().retainDeletedTombstone();

    assertThatThrownBy(() -> deleted.updateProfile(DisplayName.of("Restored person"), null))
        .isInstanceOf(AccountUserLifecycleException.class)
        .hasMessage("deleted account tombstone cannot be changed");
    assertThatThrownBy(() -> deleted.synchronizeProviderEmail(EmailAddress.of("restored@example.com")))
        .isInstanceOf(AccountUserLifecycleException.class)
        .hasMessage("deleted account tombstone cannot be changed");
  }

  @Test
  void deletedStatusRequiresCanonicalAnonymizedTombstone() {
    final var user = accountUser();

    assertThatThrownBy(() -> new AccountUser(
        user.userId(),
        user.identityProvider(),
        user.externalSubject(),
        user.email(),
        user.displayName(),
        user.avatarUrl(),
        AccountUserStatus.DELETED
    ))
        .isInstanceOf(AccountUserLifecycleException.class)
        .hasMessage("deleted account must contain only its canonical anonymized tombstone");
  }

  @Test
  void deletedIdentityCannotBeReconstructedWithAnActiveStatus() {
    final var deleted = accountUser().retainDeletedTombstone();

    assertThatThrownBy(() -> new AccountUser(
        deleted.userId(),
        deleted.identityProvider(),
        deleted.externalSubject(),
        deleted.email(),
        deleted.displayName(),
        deleted.avatarUrl(),
        AccountUserStatus.ACTIVE
    ))
        .isInstanceOf(AccountUserLifecycleException.class)
        .hasMessage("deleted account identity cannot be restored to an active lifecycle");
  }

  private static AccountUser accountUser() {
    return AccountUser.create(
        IdentityProviderCode.KEYCLOAK,
        ExternalUserSubject.of("provider-user-44"),
        EmailAddress.of("person@example.com"),
        DisplayName.of("Person Name"),
        HttpUrl.of("https://example.com/avatar.png")
    );
  }
}
