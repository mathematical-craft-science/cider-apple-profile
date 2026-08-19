package org.craft.mcs.cap.core.feedstock.planning.application.authorization.port.out;

import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

/** Authorizes Feedstock planning scenarios without exposing batch-context authorization ports. */
public interface FeedstockPlanningAccessControl {

  void requireReadAccess(@NonNull WorkspaceId workspaceId);
}
