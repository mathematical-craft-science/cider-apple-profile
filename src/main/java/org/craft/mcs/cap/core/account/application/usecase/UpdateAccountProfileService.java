package org.craft.mcs.cap.core.account.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.application.AccountNotFoundException;
import org.craft.mcs.cap.core.account.application.port.in.AccountUserView;
import org.craft.mcs.cap.core.account.application.port.in.UpdateAccountProfileCommand;
import org.craft.mcs.cap.core.account.application.port.in.UpdateAccountProfileUseCase;
import org.craft.mcs.cap.core.account.application.port.out.AccountUserRepository;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Updates only CAP-owned account profile fields. */
@Component
@RequiredArgsConstructor
public final class UpdateAccountProfileService implements UpdateAccountProfileUseCase {

  private final @NonNull CurrentAccountUserResolver currentAccountUserResolver;
  private final @NonNull AccountUserRepository accountUserRepository;
  private final @NonNull TransactionHelper transactionHelper;

  @Override
  public @NonNull AccountUserView update(@NonNull UpdateAccountProfileCommand command) {
    requireNonNull(command, "update account profile command cannot be null");
    final var currentUser = currentAccountUserResolver.resolve();

    return transactionHelper.callInTransactionNonNull(() -> {
      final var user = accountUserRepository.findById(currentUser.userId())
          .orElseThrow(AccountNotFoundException::new)
          .updateProfile(command.displayName(), command.avatarUrl());
      return AccountViewMapper.toView(accountUserRepository.save(user));
    });
  }
}
