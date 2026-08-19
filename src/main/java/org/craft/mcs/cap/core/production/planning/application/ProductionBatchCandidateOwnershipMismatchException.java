package org.craft.mcs.cap.core.production.planning.application;

import java.io.Serial;
import org.craft.mcs.cap.shared.application.ApplicationForbiddenException;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Prevents a production candidate from crossing its owning workspace boundary. */
@SuppressWarnings("java:S110")
public final class ProductionBatchCandidateOwnershipMismatchException extends ApplicationForbiddenException {

  @Serial
  private static final long serialVersionUID = 1L;

  public ProductionBatchCandidateOwnershipMismatchException(
      @NonNull WorkspaceId expectedWorkspaceId,
      @NonNull WorkspaceId actualWorkspaceId
  ) {
    requireNonNull(expectedWorkspaceId, "expected workspace id cannot be null");
    requireNonNull(actualWorkspaceId, "actual workspace id cannot be null");
    super("production batch candidate belongs to another workspace");
  }
}
