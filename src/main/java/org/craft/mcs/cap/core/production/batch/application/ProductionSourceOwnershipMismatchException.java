package org.craft.mcs.cap.core.production.batch.application;

import java.io.Serial;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.shared.application.ApplicationForbiddenException;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@SuppressWarnings("java:S110")
public final class ProductionSourceOwnershipMismatchException
    extends ApplicationForbiddenException {

  @Serial
  private static final long serialVersionUID = 1L;

  private final transient @NonNull ProductionSourceReference sourceReference;
  private final transient @NonNull WorkspaceId expectedWorkspaceId;
  private final transient @NonNull WorkspaceId actualWorkspaceId;

  public ProductionSourceOwnershipMismatchException(
      @NonNull ProductionSourceReference sourceReference,
      @NonNull WorkspaceId expectedWorkspaceId,
      @NonNull WorkspaceId actualWorkspaceId
  ) {
    super("production source belongs to another workspace: "
        + requireNonNull(sourceReference, "production source reference cannot be null").id());
    this.sourceReference = sourceReference;
    this.expectedWorkspaceId = requireNonNull(expectedWorkspaceId, "expected workspace id cannot be null");
    this.actualWorkspaceId = requireNonNull(actualWorkspaceId, "actual workspace id cannot be null");
  }

  public @NonNull ProductionSourceReference sourceReference() {
    return sourceReference;
  }

  public @NonNull WorkspaceId expectedWorkspaceId() {
    return expectedWorkspaceId;
  }

  public @NonNull WorkspaceId actualWorkspaceId() {
    return actualWorkspaceId;
  }
}
