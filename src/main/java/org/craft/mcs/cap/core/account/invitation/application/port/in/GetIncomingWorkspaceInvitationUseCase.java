package org.craft.mcs.cap.core.account.invitation.application.port.in;

import org.craft.mcs.cap.core.account.invitation.domain.model.value.WorkspaceInvitationId;
import org.jspecify.annotations.NonNull;

/** Reads an invitation preview only for the authenticated account addressed by that invitation. */
public interface GetIncomingWorkspaceInvitationUseCase {

  @NonNull IncomingWorkspaceInvitationView get(@NonNull WorkspaceInvitationId invitationId);
}
