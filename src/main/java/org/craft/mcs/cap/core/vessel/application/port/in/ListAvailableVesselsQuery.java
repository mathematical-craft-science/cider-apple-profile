package org.craft.mcs.cap.core.vessel.application.port.in;

import org.craft.mcs.cap.shared.application.query.TextSearchCriteria;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Selection criteria for operational empty vessels in one workspace. */
public record ListAvailableVesselsQuery(
    @NonNull WorkspaceId workspaceId,
    @Nullable TextSearchCriteria nameContains
) {

  public ListAvailableVesselsQuery {
    requireNonNull(workspaceId, "workspace id cannot be null");
  }
}
