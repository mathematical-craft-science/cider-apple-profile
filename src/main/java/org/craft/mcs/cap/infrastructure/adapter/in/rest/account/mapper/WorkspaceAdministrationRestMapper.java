package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.mapper;

import java.util.List;
import org.craft.mcs.cap.core.account.audit.application.port.in.WorkspaceAdministrativeAuditView;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeAuditValue;
import org.craft.mcs.cap.core.account.invitation.application.port.in.InviteWorkspaceMemberCommand;
import org.craft.mcs.cap.core.account.invitation.application.port.in.IncomingWorkspaceInvitationView;
import org.craft.mcs.cap.core.account.invitation.application.port.in.WorkspaceInvitationView;
import org.craft.mcs.cap.core.account.membership.application.port.in.ChangeWorkspaceMemberRoleCommand;
import org.craft.mcs.cap.core.account.membership.application.port.in.TransferWorkspaceOwnershipCommand;
import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.membership.domain.model.value.WorkspaceMembershipId;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.workspace.application.port.in.CreateWorkspaceCommand;
import org.craft.mcs.cap.core.account.workspace.application.port.in.UpdateWorkspaceCommand;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.ChangeWorkspaceMemberRoleRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.CreateWorkspaceRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.InviteWorkspaceMemberRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.IncomingWorkspaceInvitationListResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.IncomingWorkspaceInvitationResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.TransferWorkspaceOwnershipRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.UpdateWorkspaceRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.WorkspaceAdministrativeAuditListResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.WorkspaceAdministrativeAuditResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.WorkspaceAssignableRoleCode;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.WorkspaceInvitationListResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.WorkspaceInvitationInviterResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.WorkspaceInvitationResponse;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/** Maps workspace administration transport contracts to application commands and read models. */
public final class WorkspaceAdministrationRestMapper {

  private WorkspaceAdministrationRestMapper() {
  }

  public static @NonNull CreateWorkspaceCommand toCommand(@NonNull CreateWorkspaceRequest request) {
    requireNonNull(request, "create workspace request cannot be null");
    return new CreateWorkspaceCommand(DisplayName.of(request.name()), request.type());
  }

  public static @NonNull UpdateWorkspaceCommand toCommand(
      @NonNull WorkspaceId workspaceId,
      @NonNull UpdateWorkspaceRequest request
  ) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(request, "update workspace request cannot be null");
    return new UpdateWorkspaceCommand(workspaceId, DisplayName.of(request.name()), request.type());
  }

  public static @NonNull InviteWorkspaceMemberCommand toCommand(
      @NonNull WorkspaceId workspaceId,
      @NonNull InviteWorkspaceMemberRequest request
  ) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(request, "invite workspace member request cannot be null");
    return new InviteWorkspaceMemberCommand(
        workspaceId,
        EmailAddress.of(request.email()),
        toDomainRole(request.role())
    );
  }

  public static @NonNull ChangeWorkspaceMemberRoleCommand toCommand(
      @NonNull WorkspaceId workspaceId,
      @NonNull WorkspaceMembershipId membershipId,
      @NonNull ChangeWorkspaceMemberRoleRequest request
  ) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(membershipId, "workspace membership id cannot be null");
    requireNonNull(request, "change workspace member role request cannot be null");
    return new ChangeWorkspaceMemberRoleCommand(
        workspaceId,
        membershipId,
        toDomainRole(request.role())
    );
  }

  public static @NonNull TransferWorkspaceOwnershipCommand toCommand(
      @NonNull WorkspaceId workspaceId,
      @NonNull TransferWorkspaceOwnershipRequest request
  ) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(request, "transfer workspace ownership request cannot be null");
    return new TransferWorkspaceOwnershipCommand(
        workspaceId,
        WorkspaceMembershipId.of(request.targetMembershipId())
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

  public static @NonNull WorkspaceInvitationListResponse toInvitationListResponse(
      @NonNull List<@NonNull WorkspaceInvitationView> views
  ) {
    return new WorkspaceInvitationListResponse(views.stream().map(
        WorkspaceAdministrationRestMapper::toResponse
    ).toList());
  }

  public static @NonNull IncomingWorkspaceInvitationResponse toResponse(
      @NonNull IncomingWorkspaceInvitationView view
  ) {
    return new IncomingWorkspaceInvitationResponse(
        view.invitationId().value(),
        AccountRestMapper.toResponse(view.workspace()),
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

  public static @NonNull IncomingWorkspaceInvitationListResponse toIncomingInvitationListResponse(
      @NonNull List<@NonNull IncomingWorkspaceInvitationView> views
  ) {
    return new IncomingWorkspaceInvitationListResponse(
        views.stream()
            .map(WorkspaceAdministrationRestMapper::toResponse)
            .toList()
    );
  }

  public static @NonNull WorkspaceAdministrativeAuditResponse toResponse(
      @NonNull WorkspaceAdministrativeAuditView view
  ) {
    return new WorkspaceAdministrativeAuditResponse(
        view.auditEntryId().value(),
        view.actorUserId().value(),
        view.action(),
        view.subjectType(),
        view.subjectId().value(),
        auditValue(view.previousValue()),
        auditValue(view.newValue()),
        view.occurredAt()
    );
  }

  public static @NonNull WorkspaceAdministrativeAuditListResponse toAuditListResponse(
      @NonNull List<@NonNull WorkspaceAdministrativeAuditView> views
  ) {
    return new WorkspaceAdministrativeAuditListResponse(views.stream().map(
        WorkspaceAdministrationRestMapper::toResponse
    ).toList());
  }

  private static @Nullable String auditValue(@Nullable WorkspaceAdministrativeAuditValue value) {
    return value == null ? null : value.value();
  }

  private static @NonNull AccountRole toDomainRole(@NonNull WorkspaceAssignableRoleCode role) {
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
