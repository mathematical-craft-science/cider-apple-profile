package org.craft.mcs.cap.core.account.user.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.user.application.port.in.AccountUserView;
import org.craft.mcs.cap.core.account.user.application.port.in.GetCurrentAccountProfileUseCase;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

/** Reads CAP-owned profile data without requiring a workspace selection. */
@Component
@RequiredArgsConstructor
public final class GetCurrentAccountProfileService implements GetCurrentAccountProfileUseCase {

  private final @NonNull CurrentAccountUserResolver currentAccountUserResolver;

  @Override
  public @NonNull AccountUserView get() {
    return AccountUserView.from(currentAccountUserResolver.resolve());
  }
}
