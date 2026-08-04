package org.craft.mcs.cap.core.account.user.application.port.out;

import java.util.Optional;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUser;
import org.craft.mcs.cap.core.account.user.domain.model.value.ExternalUserSubject;
import org.craft.mcs.cap.core.account.shared.domain.value.IdentityProviderCode;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregateRepository;
import org.jspecify.annotations.NonNull;

/**
 * Aggregate repository port for CAP account users.
 *
 * <p>Physical deletion is intentionally absent. Deleted accounts are anonymized and retained as
 * tombstones so immutable audit and operation history can continue resolving their actor IDs.
 */
public interface AccountUserRepository
    extends VersionedAggregateRepository<AccountUser, AccountUserId> {

  @NonNull Optional<@NonNull AccountUser> findByEmail(@NonNull EmailAddress email);

  @NonNull Optional<@NonNull AccountUser> findByExternalIdentity(
      @NonNull IdentityProviderCode provider,
      @NonNull ExternalUserSubject subject
  );

  @NonNull Optional<@NonNull VersionedAggregate<AccountUser>> findVersionedByExternalIdentity(
      @NonNull IdentityProviderCode provider,
      @NonNull ExternalUserSubject subject
  );
}
