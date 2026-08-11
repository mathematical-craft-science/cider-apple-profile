package org.craft.mcs.cap.core.production.application;

import java.io.Serial;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.shared.application.ApplicationForbiddenException;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Indicates that a production batch mutation was requested through another workspace. */
@SuppressWarnings("java:S110")
public final class ProductionBatchOwnershipMismatchException extends ApplicationForbiddenException {

  @Serial
  private static final long serialVersionUID = 1L;

  private final transient @NonNull ProductionBatchId batchId;
  private final transient @NonNull WorkspaceId expectedWorkspaceId;
  private final transient @NonNull WorkspaceId actualWorkspaceId;

  public ProductionBatchOwnershipMismatchException(
      @NonNull ProductionBatchId batchId,
      @NonNull WorkspaceId expectedWorkspaceId,
      @NonNull WorkspaceId actualWorkspaceId
  ) {
    super("production batch belongs to another workspace: "
        + requireNonNull(batchId, "production batch id cannot be null").value());
    this.batchId = batchId;
    this.expectedWorkspaceId = requireNonNull(expectedWorkspaceId, "expected workspace id cannot be null");
    this.actualWorkspaceId = requireNonNull(actualWorkspaceId, "actual workspace id cannot be null");
  }

  public @NonNull ProductionBatchId batchId() {
    return batchId;
  }

  public @NonNull WorkspaceId expectedWorkspaceId() {
    return expectedWorkspaceId;
  }

  public @NonNull WorkspaceId actualWorkspaceId() {
    return actualWorkspaceId;
  }
}
