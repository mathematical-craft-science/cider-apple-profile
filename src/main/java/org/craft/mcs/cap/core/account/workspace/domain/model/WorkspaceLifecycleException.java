package org.craft.mcs.cap.core.account.workspace.domain.model;

import java.io.Serial;
import org.craft.mcs.cap.shared.kernel.DomainException;
import org.jspecify.annotations.NonNull;

/** Raised when a workspace lifecycle transition violates its current state. */
public final class WorkspaceLifecycleException extends DomainException {

  @Serial
  private static final long serialVersionUID = 1L;

  public WorkspaceLifecycleException(@NonNull String message) {
    super(message);
  }
}
