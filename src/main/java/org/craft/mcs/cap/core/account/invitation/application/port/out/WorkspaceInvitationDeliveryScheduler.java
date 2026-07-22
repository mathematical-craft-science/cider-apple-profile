package org.craft.mcs.cap.core.account.invitation.application.port.out;

import org.craft.mcs.cap.core.account.invitation.application.model.WorkspaceInvitationDeliveryRequest;
import org.jspecify.annotations.NonNull;

/** Schedules invitation delivery atomically with the invitation lifecycle transaction. */
public interface WorkspaceInvitationDeliveryScheduler {

  void schedule(@NonNull WorkspaceInvitationDeliveryRequest request);
}
