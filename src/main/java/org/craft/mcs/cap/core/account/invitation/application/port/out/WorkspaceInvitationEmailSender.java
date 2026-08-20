package org.craft.mcs.cap.core.account.invitation.application.port.out;

import org.craft.mcs.cap.core.account.invitation.application.contract.WorkspaceInvitationEmail;
import org.jspecify.annotations.NonNull;

/** Sends a prepared invitation email without exposing a concrete email provider to core. */
public interface WorkspaceInvitationEmailSender {

  void send(@NonNull WorkspaceInvitationEmail email);
}
