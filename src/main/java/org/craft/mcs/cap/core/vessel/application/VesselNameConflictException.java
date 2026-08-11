package org.craft.mcs.cap.core.vessel.application;

import org.craft.mcs.cap.shared.application.ApplicationConflictException;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Raised when a workspace already contains a vessel with the requested name. */
@SuppressWarnings("java:S110")
public final class VesselNameConflictException extends ApplicationConflictException {

  public VesselNameConflictException(
      @NonNull WorkspaceId workspaceId,
      @NonNull String name
  ) {
    super("workspace %s already contains vessel named %s".formatted(
        requireNonNull(workspaceId, "workspace id cannot be null").value(),
        requireNonNull(name, "vessel name cannot be null")
    ));
  }
}
