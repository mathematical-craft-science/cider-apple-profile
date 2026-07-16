package org.craft.mcs.cap.core.account.invitation.application.port.in;

import java.util.List;
import org.jspecify.annotations.NonNull;

/** Lists workspace invitations addressed to the authenticated CAP account. */
public interface ListIncomingWorkspaceInvitationsUseCase {

  @NonNull List<@NonNull IncomingWorkspaceInvitationView> list();
}
