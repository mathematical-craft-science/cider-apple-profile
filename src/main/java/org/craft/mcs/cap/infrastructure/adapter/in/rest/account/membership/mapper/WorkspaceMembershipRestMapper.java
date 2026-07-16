package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.membership.mapper;

import org.craft.mcs.cap.core.account.administration.application.port.in.ChangeWorkspaceMemberRoleCommand;
import org.craft.mcs.cap.core.account.administration.application.port.in.TransferWorkspaceOwnershipCommand;
import org.craft.mcs.cap.core.account.administration.application.port.in.WorkspaceMemberView;
import org.craft.mcs.cap.core.account.context.application.port.in.WorkspaceMembershipView;
import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.membership.domain.model.value.WorkspaceMembershipId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.membership.dto.AccountMembershipResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.membership.dto.AccountWorkspaceMemberResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.membership.dto.ChangeWorkspaceMemberRoleRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.membership.dto.TransferWorkspaceOwnershipRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.membership.dto.WorkspaceAssignableRoleCode;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.user.mapper.AccountUserRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.workspace.mapper.WorkspaceRestMapper;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Maps workspace membership, role, and ownership contracts. */
public final class WorkspaceMembershipRestMapper {

  private WorkspaceMembershipRestMapper() {
  }

  public static @NonNull AccountMembershipResponse toResponse(@NonNull WorkspaceMembershipView view) {
    return new AccountMembershipResponse(
        view.membershipId().value(),
        WorkspaceRestMapper.toResponse(view.workspace()),
        view.role(),
        view.status()
    );
  }

  public static @NonNull AccountWorkspaceMemberResponse toResponse(@NonNull WorkspaceMemberView view) {
    return new AccountWorkspaceMemberResponse(
        view.membershipId().value(),
        AccountUserRestMapper.toResponse(view.user()),
        view.role(),
        view.status()
    );
  }

  public static @NonNull ChangeWorkspaceMemberRoleCommand toCommand(
      @NonNull WorkspaceId workspaceId,
      @NonNull WorkspaceMembershipId membershipId,
      @NonNull ChangeWorkspaceMemberRoleRequest request
  ) {
    return new ChangeWorkspaceMemberRoleCommand(
        requireNonNull(workspaceId, "workspace id cannot be null"),
        requireNonNull(membershipId, "workspace membership id cannot be null"),
        toDomainRole(request.role())
    );
  }

  public static @NonNull TransferWorkspaceOwnershipCommand toCommand(
      @NonNull WorkspaceId workspaceId,
      @NonNull TransferWorkspaceOwnershipRequest request
  ) {
    return new TransferWorkspaceOwnershipCommand(
        requireNonNull(workspaceId, "workspace id cannot be null"),
        WorkspaceMembershipId.of(request.targetMembershipId())
    );
  }

  public static @NonNull AccountRole toDomainRole(@NonNull WorkspaceAssignableRoleCode role) {
    return switch (requireNonNull(role, "assignable workspace role cannot be null")) {
      case ADMIN -> AccountRole.ADMIN;
      case TECHNOLOGIST -> AccountRole.TECHNOLOGIST;
      case CIDERMAKER -> AccountRole.CIDERMAKER;
      case CATALOG_CONTRIBUTOR -> AccountRole.CATALOG_CONTRIBUTOR;
      case LAB_CONTRIBUTOR -> AccountRole.LAB_CONTRIBUTOR;
      case CURATOR -> AccountRole.CURATOR;
      case VIEWER -> AccountRole.VIEWER;
    };
  }
}
