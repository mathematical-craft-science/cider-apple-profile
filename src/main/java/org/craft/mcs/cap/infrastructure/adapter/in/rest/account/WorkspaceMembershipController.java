package org.craft.mcs.cap.infrastructure.adapter.in.rest.account;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.membership.application.port.in.WorkspaceMemberCommand;
import org.craft.mcs.cap.core.account.membership.application.port.in.WorkspaceMembershipManagementUseCase;
import org.craft.mcs.cap.core.account.membership.domain.model.value.WorkspaceMembershipId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.AccountWorkspaceMemberResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.ChangeWorkspaceMemberRoleRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.TransferWorkspaceOwnershipRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.mapper.AccountRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.mapper.WorkspaceAdministrationRestMapper;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Thin REST adapter for workspace membership and ownership commands. */
@RestController
@RequiredArgsConstructor
public final class WorkspaceMembershipController implements WorkspaceMembershipApi {

  private final @NonNull WorkspaceMembershipManagementUseCase membershipManagementUseCase;

  @Override
  @PatchMapping("/account/workspaces/{workspaceId}/members/{membershipId}/role")
  public @NonNull ResponseEntity<@NonNull AccountWorkspaceMemberResponse> changeRole(
      @PathVariable @NonNull UUID workspaceId,
      @PathVariable @NonNull UUID membershipId,
      @Valid @RequestBody @NonNull ChangeWorkspaceMemberRoleRequest request
  ) {
    final var updated = membershipManagementUseCase.changeRole(WorkspaceAdministrationRestMapper.toCommand(
        WorkspaceId.of(workspaceId),
        WorkspaceMembershipId.of(membershipId),
        request
    ));
    return ResponseEntity.ok(AccountRestMapper.toResponse(updated));
  }

  @Override
  @PostMapping("/account/workspaces/{workspaceId}/members/{membershipId}/suspend")
  public @NonNull ResponseEntity<@NonNull AccountWorkspaceMemberResponse> suspend(
      @PathVariable @NonNull UUID workspaceId,
      @PathVariable @NonNull UUID membershipId
  ) {
    return ResponseEntity.ok(AccountRestMapper.toResponse(
        membershipManagementUseCase.suspend(command(workspaceId, membershipId))
    ));
  }

  @Override
  @PostMapping("/account/workspaces/{workspaceId}/members/{membershipId}/reactivate")
  public @NonNull ResponseEntity<@NonNull AccountWorkspaceMemberResponse> reactivate(
      @PathVariable @NonNull UUID workspaceId,
      @PathVariable @NonNull UUID membershipId
  ) {
    return ResponseEntity.ok(AccountRestMapper.toResponse(
        membershipManagementUseCase.reactivate(command(workspaceId, membershipId))
    ));
  }

  @Override
  @DeleteMapping("/account/workspaces/{workspaceId}/members/{membershipId}")
  public @NonNull ResponseEntity<@NonNull Void> remove(
      @PathVariable @NonNull UUID workspaceId,
      @PathVariable @NonNull UUID membershipId
  ) {
    membershipManagementUseCase.remove(command(workspaceId, membershipId));
    return ResponseEntity.noContent().build();
  }

  @Override
  @PostMapping("/account/workspaces/{workspaceId}/leave")
  public @NonNull ResponseEntity<@NonNull Void> leave(@PathVariable @NonNull UUID workspaceId) {
    membershipManagementUseCase.leave(WorkspaceId.of(workspaceId));
    return ResponseEntity.noContent().build();
  }

  @Override
  @PostMapping("/account/workspaces/{workspaceId}/ownership-transfer")
  public @NonNull ResponseEntity<@NonNull AccountWorkspaceMemberResponse> transferOwnership(
      @PathVariable @NonNull UUID workspaceId,
      @Valid @RequestBody @NonNull TransferWorkspaceOwnershipRequest request
  ) {
    final var newOwner = membershipManagementUseCase.transferOwnership(
        WorkspaceAdministrationRestMapper.toCommand(WorkspaceId.of(workspaceId), request)
    );
    return ResponseEntity.ok(AccountRestMapper.toResponse(newOwner));
  }

  private static @NonNull WorkspaceMemberCommand command(
      @NonNull UUID workspaceId,
      @NonNull UUID membershipId
  ) {
    return new WorkspaceMemberCommand(
        WorkspaceId.of(workspaceId),
        WorkspaceMembershipId.of(membershipId)
    );
  }
}
