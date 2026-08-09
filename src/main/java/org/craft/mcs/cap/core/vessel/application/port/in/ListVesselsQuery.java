package org.craft.mcs.cap.core.vessel.application.port.in;

import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Query for the complete vessel catalog of one workspace. */
public record ListVesselsQuery(@NonNull WorkspaceId workspaceId) {

  public ListVesselsQuery {
    requireNonNull(workspaceId, "workspace id cannot be null");
  }
}
