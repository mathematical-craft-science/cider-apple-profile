package org.craft.mcs.cap.core.vessel.application;

import org.craft.mcs.cap.shared.application.ApplicationForbiddenException;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Raised when a workspace attempts to use a vessel owned by another workspace. */
@SuppressWarnings("java:S110")
public final class VesselOwnershipMismatchException extends ApplicationForbiddenException {

  public VesselOwnershipMismatchException(
      @NonNull VesselId vesselId,
      @NonNull WorkspaceId requestedWorkspaceId,
      @NonNull WorkspaceId actualWorkspaceId
  ) {
    super("vessel %s belongs to workspace %s, not %s".formatted(
        requireNonNull(vesselId, "vessel id cannot be null").value(),
        requireNonNull(actualWorkspaceId, "actual workspace id cannot be null").value(),
        requireNonNull(requestedWorkspaceId, "requested workspace id cannot be null").value()
    ));
  }
}
