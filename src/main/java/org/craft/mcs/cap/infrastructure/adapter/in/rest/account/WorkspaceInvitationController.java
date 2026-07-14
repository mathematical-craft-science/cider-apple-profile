package org.craft.mcs.cap.infrastructure.adapter.in.rest.account;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.invitation.application.port.in.WorkspaceInvitationCommand;
import org.craft.mcs.cap.core.account.invitation.application.port.in.WorkspaceInvitationUseCase;
import org.craft.mcs.cap.core.account.invitation.domain.model.value.WorkspaceInvitationId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.AccountMembershipResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.InviteWorkspaceMemberRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.WorkspaceInvitationListResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.WorkspaceInvitationResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.mapper.AccountRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.mapper.WorkspaceAdministrationRestMapper;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Thin REST adapter for workspace invitation lifecycle. */
@RestController
@RequiredArgsConstructor
public final class WorkspaceInvitationController implements WorkspaceInvitationApi {

  private final @NonNull WorkspaceInvitationUseCase workspaceInvitationUseCase;

  @Override
  @PostMapping("/account/workspaces/{workspaceId}/invitations")
  public @NonNull ResponseEntity<@NonNull WorkspaceInvitationResponse> invite(
      @PathVariable @NonNull UUID workspaceId,
      @Valid @RequestBody @NonNull InviteWorkspaceMemberRequest request
  ) {
    final var invitation = workspaceInvitationUseCase.invite(WorkspaceAdministrationRestMapper.toCommand(
        WorkspaceId.of(workspaceId),
        request
    ));
    return ResponseEntity.status(201).body(WorkspaceAdministrationRestMapper.toResponse(invitation));
  }

  @Override
  @GetMapping("/account/workspaces/{workspaceId}/invitations")
  public @NonNull ResponseEntity<@NonNull WorkspaceInvitationListResponse> invitations(
      @PathVariable @NonNull UUID workspaceId
  ) {
    return ResponseEntity.ok(WorkspaceAdministrationRestMapper.toInvitationListResponse(
        workspaceInvitationUseCase.list(WorkspaceId.of(workspaceId))
    ));
  }

  @Override
  @PostMapping("/account/workspace-invitations/{invitationId}/accept")
  public @NonNull ResponseEntity<@NonNull AccountMembershipResponse> accept(
      @PathVariable @NonNull UUID invitationId
  ) {
    return ResponseEntity.ok(AccountRestMapper.toResponse(
        workspaceInvitationUseCase.accept(WorkspaceInvitationId.of(invitationId))
    ));
  }

  @Override
  @PostMapping("/account/workspace-invitations/{invitationId}/reject")
  public @NonNull ResponseEntity<@NonNull WorkspaceInvitationResponse> reject(
      @PathVariable @NonNull UUID invitationId
  ) {
    return ResponseEntity.ok(WorkspaceAdministrationRestMapper.toResponse(
        workspaceInvitationUseCase.reject(WorkspaceInvitationId.of(invitationId))
    ));
  }

  @Override
  @PostMapping("/account/workspaces/{workspaceId}/invitations/{invitationId}/revoke")
  public @NonNull ResponseEntity<@NonNull WorkspaceInvitationResponse> revoke(
      @PathVariable @NonNull UUID workspaceId,
      @PathVariable @NonNull UUID invitationId
  ) {
    return ResponseEntity.ok(WorkspaceAdministrationRestMapper.toResponse(
        workspaceInvitationUseCase.revoke(command(workspaceId, invitationId))
    ));
  }

  @Override
  @PostMapping("/account/workspaces/{workspaceId}/invitations/{invitationId}/resend")
  public @NonNull ResponseEntity<@NonNull WorkspaceInvitationResponse> resend(
      @PathVariable @NonNull UUID workspaceId,
      @PathVariable @NonNull UUID invitationId
  ) {
    return ResponseEntity.ok(WorkspaceAdministrationRestMapper.toResponse(
        workspaceInvitationUseCase.resend(command(workspaceId, invitationId))
    ));
  }

  private static @NonNull WorkspaceInvitationCommand command(
      @NonNull UUID workspaceId,
      @NonNull UUID invitationId
  ) {
    return new WorkspaceInvitationCommand(
        WorkspaceId.of(workspaceId),
        WorkspaceInvitationId.of(invitationId)
    );
  }
}
