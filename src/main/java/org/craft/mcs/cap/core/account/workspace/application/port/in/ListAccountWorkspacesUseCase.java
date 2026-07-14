package org.craft.mcs.cap.core.account.workspace.application.port.in;

import java.util.List;
import org.craft.mcs.cap.core.account.membership.application.port.in.WorkspaceMembershipView;
import org.jspecify.annotations.NonNull;

/** Lists workspaces accessible to the current CAP account. */
public interface ListAccountWorkspacesUseCase {

  @NonNull List<@NonNull WorkspaceMembershipView> list();
}
