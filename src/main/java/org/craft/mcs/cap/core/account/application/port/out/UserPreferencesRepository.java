package org.craft.mcs.cap.core.account.application.port.out;

import java.util.Optional;
import org.craft.mcs.cap.core.account.domain.model.UserPreferences;
import org.craft.mcs.cap.core.account.domain.model.value.AccountUserId;
import org.jspecify.annotations.NonNull;


/** Aggregate repository port for provider-independent user preferences. */
public interface UserPreferencesRepository {

  @NonNull UserPreferences save(@NonNull UserPreferences preferences);

  @NonNull Optional<@NonNull UserPreferences> findByUserId(@NonNull AccountUserId userId);
}
