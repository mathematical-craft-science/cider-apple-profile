package org.craft.mcs.cap.core.account.context.application.usecase;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.context.application.port.in.WorkspaceMembershipView;
import org.craft.mcs.cap.core.account.membership.application.port.out.WorkspaceMembershipRepository;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.workspace.application.WorkspaceNotFoundException;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceRepository;
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

  private final @NonNull WorkspaceMembershipRepository membershipRepository;
  private final @NonNull WorkspaceRepository workspaceRepository;

  public @NonNull List<@NonNull WorkspaceMembershipView> readActive(@NonNull AccountUserId userId) {
    return membershipRepository.findAllByUserId(userId).stream()
        .filter(WorkspaceMembership::isActive)
        .map(this::toActiveWorkspaceView)
        .flatMap(Optional::stream)
        .sorted(Comparator.comparing(view -> view.workspace().name().value()))
        .toList();
  }

  private @NonNull Optional<@NonNull WorkspaceMembershipView> toActiveWorkspaceView(
      @NonNull WorkspaceMembership membership
  ) {
    final var workspace = workspaceRepository.findById(membership.workspaceId())
        .orElseThrow(() -> new WorkspaceNotFoundException(membership.workspaceId()));
    return workspace.isActive()
        ? Optional.of(WorkspaceMembershipView.from(membership, workspace))
        : Optional.empty();
  }
}
