package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.context.mapper;

import org.craft.mcs.cap.core.account.context.application.port.in.AccountMeView;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.context.dto.AccountMeResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.membership.mapper.WorkspaceMembershipRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.user.mapper.AccountUserRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.workspace.mapper.WorkspaceRestMapper;
import org.jspecify.annotations.NonNull;

/** Maps the authenticated account context assembled from user and workspace views. */
public final class AccountContextRestMapper {

  private AccountContextRestMapper() {
  }

  public static @NonNull AccountMeResponse toResponse(@NonNull AccountMeView view) {
    return new AccountMeResponse(
        AccountUserRestMapper.toResponse(view.user()),
        WorkspaceRestMapper.toResponse(view.activeWorkspace()),
        view.memberships().stream().map(WorkspaceMembershipRestMapper::toResponse).toList(),
        AccountUserRestMapper.toResponse(view.preferences()),
        view.personalCapabilities(),
        view.workspacePermissions()
    );
  }
}
