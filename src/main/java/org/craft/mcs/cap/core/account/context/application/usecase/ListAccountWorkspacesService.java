package org.craft.mcs.cap.core.account.context.application.usecase;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.context.application.port.in.ListAccountWorkspacesUseCase;
import org.craft.mcs.cap.core.account.context.application.port.in.WorkspaceMembershipView;
import org.craft.mcs.cap.core.account.user.application.usecase.CurrentAccountUserResolver;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;


/** Lists active current-account workspaces without requiring an active workspace selection. */
@Component
@RequiredArgsConstructor
public final class ListAccountWorkspacesService implements ListAccountWorkspacesUseCase {

  private final @NonNull CurrentAccountUserResolver currentAccountUserResolver;
  private final @NonNull CurrentAccountMembershipReader currentAccountMembershipReader;

  @Override
  public @NonNull List<@NonNull WorkspaceMembershipView> list() {
    final var user = currentAccountUserResolver.resolve();
    return currentAccountMembershipReader.readActive(user.userId());
  }
}
