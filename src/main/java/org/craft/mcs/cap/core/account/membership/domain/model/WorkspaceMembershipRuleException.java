package org.craft.mcs.cap.core.account.membership.domain.model;

import java.io.Serial;
import org.craft.mcs.cap.shared.kernel.DomainException;
import org.jspecify.annotations.NonNull;

/** Raised when a membership or ownership transition would violate collaboration invariants. */
public final class WorkspaceMembershipRuleException extends DomainException {

  @Serial
  private static final long serialVersionUID = 1L;

  public WorkspaceMembershipRuleException(@NonNull String message) {
    super(message);
  }
}
