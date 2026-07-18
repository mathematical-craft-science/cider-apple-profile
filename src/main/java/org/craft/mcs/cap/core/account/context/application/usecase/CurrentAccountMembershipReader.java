package org.craft.mcs.cap.core.account.context.application.usecase;

import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.context.application.model.WorkspaceMembershipSnapshot;
import org.craft.mcs.cap.core.account.context.application.port.in.WorkspaceMembershipView;
import org.craft.mcs.cap.core.account.context.application.port.out.CurrentAccountWorkspaceQuery;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.workspace.application.port.in.WorkspaceView;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

/**
 * Builds a deterministic display list of active workspace memberships.
 *
 * <p>The ordering is presentation-oriented and must never be used to select the active workspace.
 */
@Component
@RequiredArgsConstructor
public final class CurrentAccountMembershipReader {

  private final @NonNull CurrentAccountWorkspaceQuery currentAccountWorkspaceQuery;

  public @NonNull List<@NonNull WorkspaceMembershipView> readActive(@NonNull AccountUserId userId) {
    return currentAccountWorkspaceQuery.findActiveByUserId(userId).stream()
        .map(CurrentAccountMembershipReader::toView)
        .sorted(Comparator.comparing(view -> view.workspace().name().value()))
        .toList();
  }

  private static @NonNull WorkspaceMembershipView toView(
      @NonNull WorkspaceMembershipSnapshot snapshot
  ) {
    return new WorkspaceMembershipView(
        snapshot.membershipId(),
        new WorkspaceView(
            snapshot.workspaceId(),
            snapshot.workspaceName(),
            snapshot.workspaceType(),
            snapshot.workspaceStatus()
        ),
        snapshot.role(),
        snapshot.membershipStatus()
    );
  }
}
