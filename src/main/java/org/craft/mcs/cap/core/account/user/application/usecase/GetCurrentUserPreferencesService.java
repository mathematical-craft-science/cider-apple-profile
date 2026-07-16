package org.craft.mcs.cap.core.account.user.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.user.application.port.in.GetCurrentUserPreferencesUseCase;
import org.craft.mcs.cap.core.account.user.application.port.in.UserPreferencesView;
import org.craft.mcs.cap.core.account.user.application.port.out.UserPreferencesRepository;
import org.craft.mcs.cap.core.account.user.domain.model.UserPreferences;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

/** Reads personal preferences without requiring a workspace selection. */
@Component
@RequiredArgsConstructor
public final class GetCurrentUserPreferencesService implements GetCurrentUserPreferencesUseCase {

  private final @NonNull CurrentAccountUserResolver currentAccountUserResolver;
  private final @NonNull UserPreferencesRepository userPreferencesRepository;

  @Override
  public @NonNull UserPreferencesView get() {
    final var user = currentAccountUserResolver.resolve();
    final var preferences = userPreferencesRepository.findByUserId(user.userId())
        .orElseGet(() -> UserPreferences.defaults(user.userId()));
    return UserPreferencesView.from(preferences);
  }
}
