package org.craft.mcs.cap.core.account.invitation.application.port.in;

import java.util.List;
import org.craft.mcs.cap.core.account.invitation.domain.model.value.WorkspaceInvitationId;
import org.craft.mcs.cap.core.account.context.application.port.in.WorkspaceMembershipView;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

/** Manages workspace invitation lifecycle independently from registered memberships. */
public interface WorkspaceInvitationUseCase {

  @NonNull WorkspaceInvitationView invite(@NonNull InviteWorkspaceMemberCommand command);

  @NonNull List<@NonNull WorkspaceInvitationView> list(@NonNull WorkspaceId workspaceId);

  @NonNull WorkspaceMembershipView accept(@NonNull WorkspaceInvitationId invitationId);

  @NonNull WorkspaceInvitationView reject(@NonNull WorkspaceInvitationId invitationId);

  @NonNull WorkspaceInvitationView revoke(@NonNull WorkspaceInvitationCommand command);

  @NonNull WorkspaceInvitationView resend(@NonNull WorkspaceInvitationCommand command);
}
