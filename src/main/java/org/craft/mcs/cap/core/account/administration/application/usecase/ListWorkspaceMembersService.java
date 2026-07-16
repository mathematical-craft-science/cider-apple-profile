package org.craft.mcs.cap.core.account.administration.application.usecase;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.context.application.usecase.WorkspacePermissionChecker;
import org.craft.mcs.cap.core.account.administration.application.port.in.ListWorkspaceMembersUseCase;
import org.craft.mcs.cap.core.account.administration.application.port.in.WorkspaceMemberView;
import org.craft.mcs.cap.core.account.membership.application.port.out.WorkspaceMembershipRepository;
import org.craft.mcs.cap.core.account.user.application.AccountNotFoundException;
import org.craft.mcs.cap.core.account.user.application.port.in.AccountUserView;
import org.craft.mcs.cap.core.account.user.application.port.out.AccountUserRepository;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.core.account.membership.domain.model.WorkspacePermission.WORKSPACE_MEMBERS_READ;


/** Lists workspace members after a CAP membership permission check. */
@Component
@RequiredArgsConstructor
public final class ListWorkspaceMembersService implements ListWorkspaceMembersUseCase {

  private final @NonNull WorkspacePermissionChecker workspacePermissionChecker;
  private final @NonNull WorkspaceMembershipRepository membershipRepository;
  private final @NonNull AccountUserRepository accountUserRepository;

  @Override
  public @NonNull List<@NonNull WorkspaceMemberView> list(@NonNull WorkspaceId workspaceId) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    workspacePermissionChecker.requirePermission(workspaceId, WORKSPACE_MEMBERS_READ);

    return membershipRepository.findAllByWorkspaceId(workspaceId).stream()
        .map(membership -> {
          final var user = accountUserRepository.findById(membership.userId())
              .orElseThrow(AccountNotFoundException::new);
          return new WorkspaceMemberView(
              membership.membershipId(),
              AccountUserView.from(user),
              membership.role(),
              membership.status()
          );
        })
        .toList();
  }
}
