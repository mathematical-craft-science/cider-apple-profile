package org.craft.mcs.cap.core.account.application.usecase;

import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.application.WorkspaceNotFoundException;
import org.craft.mcs.cap.core.account.application.port.in.WorkspaceMembershipView;
import org.craft.mcs.cap.core.account.application.port.out.WorkspaceMembershipRepository;
import org.craft.mcs.cap.core.account.application.port.out.WorkspaceRepository;
import org.craft.mcs.cap.core.account.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.domain.model.value.AccountUserId;
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
        .map(this::toView)
        .sorted(Comparator.comparing(view -> view.workspace().name().value()))
        .toList();
  }

  private @NonNull WorkspaceMembershipView toView(@NonNull WorkspaceMembership membership) {
    final var workspace = workspaceRepository.findById(membership.workspaceId())
        .orElseThrow(() -> new WorkspaceNotFoundException(membership.workspaceId()));
    return AccountViewMapper.toView(membership, workspace);
  }
}
