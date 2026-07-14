package org.craft.mcs.cap.core.account.invitation.domain.model;

import java.io.Serial;
import org.craft.mcs.cap.shared.kernel.DomainException;
import org.jspecify.annotations.NonNull;

/** Raised when an invitation command is incompatible with its lifecycle state or intended recipient. */
public final class WorkspaceInvitationRuleException extends DomainException {

  @Serial
  private static final long serialVersionUID = 1L;

  public WorkspaceInvitationRuleException(@NonNull String message) {
    super(message);
  }
}
