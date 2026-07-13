package org.craft.mcs.cap.core.account.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.application.port.in.AccountUserView;
import org.craft.mcs.cap.core.account.application.port.in.GetCurrentAccountProfileUseCase;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;


/** Reads CAP-owned profile data without requiring a workspace selection. */
@Component
@RequiredArgsConstructor
public final class GetCurrentAccountProfileService implements GetCurrentAccountProfileUseCase {

  private final @NonNull CurrentAccountUserResolver currentAccountUserResolver;

  @Override
  public @NonNull AccountUserView get() {
    return AccountViewMapper.toView(currentAccountUserResolver.resolve());
  }
}
