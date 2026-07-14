package org.craft.mcs.cap.core.account.audit.domain.model;

import java.io.Serial;
import org.craft.mcs.cap.shared.kernel.DomainException;
import org.jspecify.annotations.NonNull;

/** Raised when an administrative audit fact lacks a valid workspace subject. */
public final class WorkspaceAdministrativeAuditRuleException extends DomainException {

  @Serial
  private static final long serialVersionUID = 1L;

  public WorkspaceAdministrativeAuditRuleException(@NonNull String message) {
    super(message);
  }
}
