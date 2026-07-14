package org.craft.mcs.cap.core.account.user.application.port.out;

import java.util.Optional;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUser;
import org.craft.mcs.cap.core.account.user.domain.model.value.ExternalUserSubject;
import org.craft.mcs.cap.core.account.shared.domain.value.IdentityProviderCode;
import org.jspecify.annotations.NonNull;

/** Aggregate repository port for CAP account users. */
public interface AccountUserRepository {

  @NonNull AccountUser save(@NonNull AccountUser user);

  @NonNull Optional<@NonNull AccountUser> findById(@NonNull AccountUserId userId);

  @NonNull Optional<@NonNull AccountUser> findByEmail(@NonNull EmailAddress email);

  @NonNull Optional<@NonNull AccountUser> findByExternalIdentity(
      @NonNull IdentityProviderCode provider,
      @NonNull ExternalUserSubject subject
  );
}
