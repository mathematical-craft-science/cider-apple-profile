package org.craft.mcs.cap.core.account.user.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.user.application.port.in.UpdateUserPreferencesCommand;
import org.craft.mcs.cap.core.account.user.application.port.in.UpdateUserPreferencesUseCase;
import org.craft.mcs.cap.core.account.user.application.port.in.UserPreferencesView;
import org.craft.mcs.cap.core.account.user.application.port.out.UserPreferencesRepository;
import org.craft.mcs.cap.core.account.user.domain.model.UserPreferences;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/** Persists locale and time-zone preferences owned by the current CAP user. */
@Component
@RequiredArgsConstructor
public final class UpdateUserPreferencesService implements UpdateUserPreferencesUseCase {

  private final @NonNull CurrentAccountUserResolver currentAccountUserResolver;
  private final @NonNull UserPreferencesRepository userPreferencesRepository;
  private final @NonNull TransactionHelper transactionHelper;

  @Override
  public @NonNull UserPreferencesView update(@NonNull UpdateUserPreferencesCommand command) {
    requireNonNull(command, "update user preferences command cannot be null");
    final var currentUser = currentAccountUserResolver.resolve();

    return transactionHelper.callInTransactionNonNull(() -> UserPreferencesView.from(
        userPreferencesRepository.save(new UserPreferences(
            currentUser.userId(),
            command.locale(),
            command.timeZone()
        ))
    ));
  }
}
