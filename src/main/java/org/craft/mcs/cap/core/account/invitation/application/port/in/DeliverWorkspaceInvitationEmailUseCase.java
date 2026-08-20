package org.craft.mcs.cap.core.account.invitation.application.port.in;

import org.craft.mcs.cap.core.account.invitation.application.contract.WorkspaceInvitationDeliveryRequest;
import org.jspecify.annotations.NonNull;

/** Delivers an invitation generation after revalidating its current lifecycle state. */
public interface DeliverWorkspaceInvitationEmailUseCase {

  void deliver(@NonNull WorkspaceInvitationDeliveryRequest request);
}
