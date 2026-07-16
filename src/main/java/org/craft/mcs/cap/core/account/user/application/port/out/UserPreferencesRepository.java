package org.craft.mcs.cap.core.account.user.application.port.out;

import java.util.Optional;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.user.domain.model.UserPreferences;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregateRepository;
import org.jspecify.annotations.NonNull;

/** Aggregate repository port for provider-independent user preferences. */
public interface UserPreferencesRepository
    extends VersionedAggregateRepository<UserPreferences, AccountUserId> {

  default @NonNull Optional<@NonNull UserPreferences> findByUserId(@NonNull AccountUserId userId) {
    return findById(userId);
  }
}
