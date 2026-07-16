package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.user;

import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.shared.domain.value.HttpUrl;
import org.craft.mcs.cap.core.account.shared.domain.value.IdentityProviderCode;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUser;
import org.craft.mcs.cap.core.account.user.domain.model.value.ExternalUserSubject;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Maps the AccountUser aggregate to its Spring Data JDBC representation. */
final class AccountUserJdbcMapper {

  private AccountUserJdbcMapper() {
  }

  static @NonNull AccountUserJdbcEntity fromDomain(@NonNull AccountUser user) {
    return new AccountUserJdbcEntity(
        user.userId().value(),
        user.identityProvider().value(),
        user.externalSubject().value(),
        user.email().value(),
        user.displayName().value(),
        user.avatarUrl() == null ? null : user.avatarUrl().value().toString(),
        user.status()
    );
  }

  static @NonNull AccountUserJdbcEntity updateFromDomain(
      @NonNull AccountUser user,
      @NonNull AccountUserJdbcEntity entity
  ) {
    entity.identityProvider = user.identityProvider().value();
    entity.externalSubject = user.externalSubject().value();
    entity.email = user.email().value();
    entity.displayName = user.displayName().value();
    entity.avatarUrl = user.avatarUrl() == null ? null : user.avatarUrl().value().toString();
    entity.status = user.status();
    return entity;
  }

  static @NonNull AccountUser toDomain(@NonNull AccountUserJdbcEntity entity) {
    return new AccountUser(
        AccountUserId.of(entity.userId),
        IdentityProviderCode.of(entity.identityProvider),
        ExternalUserSubject.of(entity.externalSubject),
        EmailAddress.of(entity.email),
        DisplayName.of(entity.displayName),
        HttpUrl.fromNullable(entity.avatarUrl),
        entity.status
    );
  }

  static @NonNull VersionedAggregate<AccountUser> toVersionedAggregate(
      @NonNull AccountUserJdbcEntity entity
  ) {
    return new VersionedAggregate<>(toDomain(entity), AggregateRevision.of(entity.version));
  }

  static long revision(@NonNull AccountUserJdbcEntity entity) {
    return requireNonNull(entity, "account user row cannot be null").version;
  }
}
