package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.invitation;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.invitation.application.port.in.GetIncomingWorkspaceInvitationUseCase;
import org.craft.mcs.cap.core.account.invitation.application.port.in.ListIncomingWorkspaceInvitationsUseCase;
import org.craft.mcs.cap.core.account.invitation.application.port.in.WorkspaceInvitationCommand;
import org.craft.mcs.cap.core.account.invitation.application.port.in.WorkspaceInvitationUseCase;
import org.craft.mcs.cap.core.account.invitation.domain.model.value.WorkspaceInvitationId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.membership.dto.AccountMembershipResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.invitation.dto.InviteWorkspaceMemberRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.invitation.dto.IncomingWorkspaceInvitationListResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.invitation.dto.IncomingWorkspaceInvitationResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.invitation.dto.WorkspaceInvitationListResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.invitation.dto.WorkspaceInvitationResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.invitation.mapper.WorkspaceInvitationRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.membership.mapper.WorkspaceMembershipRestMapper;
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
  private final @NonNull GetIncomingWorkspaceInvitationUseCase getIncomingWorkspaceInvitationUseCase;
  private final @NonNull ListIncomingWorkspaceInvitationsUseCase listIncomingWorkspaceInvitationsUseCase;

  @Override
  @GetMapping("/account/workspace-invitations")
  public @NonNull ResponseEntity<@NonNull IncomingWorkspaceInvitationListResponse> incomingInvitations() {
    return ResponseEntity.ok(WorkspaceInvitationRestMapper.toIncomingListResponse(
        listIncomingWorkspaceInvitationsUseCase.list()
    ));
  }

  @Override
  @GetMapping("/account/workspace-invitations/{invitationId}")
  public @NonNull ResponseEntity<@NonNull IncomingWorkspaceInvitationResponse> incomingInvitation(
      @PathVariable @NonNull UUID invitationId
  ) {
    return ResponseEntity.ok(WorkspaceInvitationRestMapper.toResponse(
        getIncomingWorkspaceInvitationUseCase.get(WorkspaceInvitationId.of(invitationId))
    ));
  }

  @Override
  @PostMapping("/account/workspaces/{workspaceId}/invitations")
  public @NonNull ResponseEntity<@NonNull WorkspaceInvitationResponse> invite(
      @PathVariable @NonNull UUID workspaceId,
      @Valid @RequestBody @NonNull InviteWorkspaceMemberRequest request
  ) {
    final var invitation = workspaceInvitationUseCase.invite(WorkspaceInvitationRestMapper.toCommand(
        WorkspaceId.of(workspaceId),
        request
    ));
    return ResponseEntity.status(201).body(WorkspaceInvitationRestMapper.toResponse(invitation));
  }

  @Override
  @GetMapping("/account/workspaces/{workspaceId}/invitations")
  public @NonNull ResponseEntity<@NonNull WorkspaceInvitationListResponse> invitations(
      @PathVariable @NonNull UUID workspaceId
  ) {
    return ResponseEntity.ok(WorkspaceInvitationRestMapper.toListResponse(
        workspaceInvitationUseCase.list(WorkspaceId.of(workspaceId))
    ));
  }

  @Override
  @PostMapping("/account/workspace-invitations/{invitationId}/accept")
  public @NonNull ResponseEntity<@NonNull AccountMembershipResponse> accept(
      @PathVariable @NonNull UUID invitationId
  ) {
    return ResponseEntity.ok(WorkspaceMembershipRestMapper.toResponse(
        workspaceInvitationUseCase.accept(WorkspaceInvitationId.of(invitationId))
    ));
  }

  @Override
  @PostMapping("/account/workspace-invitations/{invitationId}/reject")
  public @NonNull ResponseEntity<@NonNull WorkspaceInvitationResponse> reject(
      @PathVariable @NonNull UUID invitationId
  ) {
    return ResponseEntity.ok(WorkspaceInvitationRestMapper.toResponse(
        workspaceInvitationUseCase.reject(WorkspaceInvitationId.of(invitationId))
    ));
  }

  @Override
  @PostMapping("/account/workspaces/{workspaceId}/invitations/{invitationId}/revoke")
  public @NonNull ResponseEntity<@NonNull WorkspaceInvitationResponse> revoke(
      @PathVariable @NonNull UUID workspaceId,
      @PathVariable @NonNull UUID invitationId
  ) {
    return ResponseEntity.ok(WorkspaceInvitationRestMapper.toResponse(
        workspaceInvitationUseCase.revoke(command(workspaceId, invitationId))
    ));
  }

  @Override
  @PostMapping("/account/workspaces/{workspaceId}/invitations/{invitationId}/resend")
  public @NonNull ResponseEntity<@NonNull WorkspaceInvitationResponse> resend(
      @PathVariable @NonNull UUID workspaceId,
      @PathVariable @NonNull UUID invitationId
  ) {
    return ResponseEntity.ok(WorkspaceInvitationRestMapper.toResponse(
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
