package org.craft.mcs.cap.core.feedstock.application.authorization.port.out;

import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Outbound authorization boundary for feedstock data owned by a workspace scope. */
public interface FeedstockAccessControl {

  void requireReadAccess(@NonNull WorkspaceId workspaceId);

  void requireWriteAccess(@NonNull WorkspaceId workspaceId);
}
