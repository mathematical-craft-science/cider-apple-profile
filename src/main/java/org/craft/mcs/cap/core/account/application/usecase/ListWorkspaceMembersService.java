package org.craft.mcs.cap.core.account.application.usecase;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.application.AccountAccessDeniedException;
import org.craft.mcs.cap.core.account.application.AccountNotFoundException;
import org.craft.mcs.cap.core.account.application.port.in.ListWorkspaceMembersUseCase;
import org.craft.mcs.cap.core.account.application.port.in.WorkspaceMemberView;
import org.craft.mcs.cap.core.account.application.port.out.AccountUserRepository;
import org.craft.mcs.cap.core.account.application.port.out.WorkspaceMembershipRepository;
import org.craft.mcs.cap.core.account.domain.model.AccountPermission;
import org.craft.mcs.cap.core.account.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Lists workspace members after a CAP membership permission check. */
@Component
@RequiredArgsConstructor
public final class ListWorkspaceMembersService implements ListWorkspaceMembersUseCase {

  private final @NonNull CurrentAccountUserResolver currentAccountUserResolver;
  private final @NonNull WorkspaceMembershipRepository membershipRepository;
  private final @NonNull AccountUserRepository accountUserRepository;

  @Override
  public @NonNull List<@NonNull WorkspaceMemberView> list(@NonNull WorkspaceId workspaceId) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    final var currentUser = currentAccountUserResolver.resolve();
    final var currentMembership = membershipRepository
        .findByWorkspaceIdAndUserId(workspaceId, currentUser.userId())
        .filter(WorkspaceMembership::isActive)
        .orElseThrow(() -> new AccountAccessDeniedException("workspace membership is required"));
    if (!currentMembership.role().permissions().contains(AccountPermission.WORKSPACE_MEMBERS_READ)) {
      throw new AccountAccessDeniedException("workspace member read permission is required");
    }

    return membershipRepository.findAllByWorkspaceId(workspaceId).stream()
        .map(membership -> {
          final var user = accountUserRepository.findById(membership.userId())
              .orElseThrow(AccountNotFoundException::new);
          return new WorkspaceMemberView(
              membership.membershipId(),
              AccountViewMapper.toView(user),
              membership.role(),
              membership.status()
          );
        })
        .toList();
  }
}
