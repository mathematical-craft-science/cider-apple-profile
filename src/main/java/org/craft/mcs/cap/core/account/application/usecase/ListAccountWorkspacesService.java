package org.craft.mcs.cap.core.account.application.usecase;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.application.port.in.GetCurrentAccountUseCase;
import org.craft.mcs.cap.core.account.application.port.in.ListAccountWorkspacesUseCase;
import org.craft.mcs.cap.core.account.application.port.in.WorkspaceMembershipView;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;


/** Lists current-account workspaces from the canonical account context read model. */
@Component
@RequiredArgsConstructor
public final class ListAccountWorkspacesService implements ListAccountWorkspacesUseCase {

  private final @NonNull GetCurrentAccountUseCase getCurrentAccountUseCase;

  @Override
  public @NonNull List<@NonNull WorkspaceMembershipView> list() {
    return getCurrentAccountUseCase.get().memberships();
  }
}
