package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.invitation.mapper;

import java.util.List;
import org.craft.mcs.cap.core.account.invitation.application.port.in.IncomingWorkspaceInvitationView;
import org.craft.mcs.cap.core.account.invitation.application.port.in.InviteWorkspaceMemberCommand;
import org.craft.mcs.cap.core.account.invitation.application.port.in.WorkspaceInvitationView;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.invitation.dto.IncomingWorkspaceInvitationListResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.invitation.dto.IncomingWorkspaceInvitationResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.invitation.dto.InviteWorkspaceMemberRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.invitation.dto.WorkspaceInvitationInviterResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.invitation.dto.WorkspaceInvitationListResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.invitation.dto.WorkspaceInvitationResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.membership.mapper.WorkspaceMembershipRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.workspace.mapper.WorkspaceRestMapper;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

/** Maps workspace invitation lifecycle contracts. */
public final class WorkspaceInvitationRestMapper {

  private WorkspaceInvitationRestMapper() {
  }

  public static @NonNull InviteWorkspaceMemberCommand toCommand(
      @NonNull WorkspaceId workspaceId,
      @NonNull InviteWorkspaceMemberRequest request
  ) {
    return new InviteWorkspaceMemberCommand(
        workspaceId,
        EmailAddress.of(request.email()),
        WorkspaceMembershipRestMapper.toDomainRole(request.role())
    );
  }

  public static @NonNull WorkspaceInvitationResponse toResponse(
      @NonNull WorkspaceInvitationView view
  ) {
    return new WorkspaceInvitationResponse(
        view.invitationId().value(),
        view.workspaceId().value(),
        view.invitedEmail().value(),
        view.role(),
        view.status(),
        view.invitedByUserId().value(),
        view.acceptedByUserId() == null ? null : view.acceptedByUserId().value(), // NOSONAR
        view.expiresAt(),
        view.resendCount()
    );
  }

  public static @NonNull WorkspaceInvitationListResponse toListResponse(
      @NonNull List<@NonNull WorkspaceInvitationView> views
  ) {
    return new WorkspaceInvitationListResponse(
        views.stream().map(WorkspaceInvitationRestMapper::toResponse).toList()
    );
  }

  public static @NonNull IncomingWorkspaceInvitationResponse toResponse(
      @NonNull IncomingWorkspaceInvitationView view
  ) {
    return new IncomingWorkspaceInvitationResponse(
        view.invitationId().value(),
        WorkspaceRestMapper.toResponse(view.workspace()),
        view.role(),
        view.status(),
        new WorkspaceInvitationInviterResponse(
            view.invitedBy().userId().value(),
            view.invitedBy().displayName().value(),
            view.invitedBy().avatarUrl() == null
                ? null
                : view.invitedBy().avatarUrl().value().toString() // NOSONAR
        ),
        view.expiresAt()
    );
  }

  public static @NonNull IncomingWorkspaceInvitationListResponse toIncomingListResponse(
      @NonNull List<@NonNull IncomingWorkspaceInvitationView> views
  ) {
    return new IncomingWorkspaceInvitationListResponse(
        views.stream().map(WorkspaceInvitationRestMapper::toResponse).toList()
    );
  }
}
