package org.craft.mcs.cap.core.account.application.port.in;

import java.util.List;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Lists members after validating that the current actor can access the workspace. */
public interface ListWorkspaceMembersUseCase {

  @NonNull List<@NonNull WorkspaceMemberView> list(@NonNull WorkspaceId workspaceId);
}
