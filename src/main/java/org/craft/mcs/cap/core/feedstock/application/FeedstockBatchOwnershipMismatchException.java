package org.craft.mcs.cap.core.feedstock.application;

import java.io.Serial;
import org.craft.mcs.cap.shared.application.ApplicationForbiddenException;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@SuppressWarnings("java:S110")
public final class FeedstockBatchOwnershipMismatchException
    extends ApplicationForbiddenException {

  @Serial
  private static final long serialVersionUID = 1L;

  private final transient @NonNull FeedstockBatchId batchId;
  private final transient @NonNull WorkspaceId expectedWorkspaceId;
  private final transient @NonNull WorkspaceId actualWorkspaceId;

  public FeedstockBatchOwnershipMismatchException(
      @NonNull FeedstockBatchId batchId,
      @NonNull WorkspaceId expectedWorkspaceId,
      @NonNull WorkspaceId actualWorkspaceId
  ) {
    super("feedstock batch belongs to another workspace: "
        + requireNonNull(batchId, "feedstock batch id cannot be null").value());
    this.batchId = batchId;
    this.expectedWorkspaceId = requireNonNull(expectedWorkspaceId, "expected workspace id cannot be null");
    this.actualWorkspaceId = requireNonNull(actualWorkspaceId, "actual workspace id cannot be null");
  }

  public @NonNull FeedstockBatchId batchId() {
    return batchId;
  }

  public @NonNull WorkspaceId expectedWorkspaceId() {
    return expectedWorkspaceId;
  }

  public @NonNull WorkspaceId actualWorkspaceId() {
    return actualWorkspaceId;
  }
}
