package org.craft.mcs.cap.core.account.context.application.port.in;

import org.craft.mcs.cap.core.account.membership.domain.model.WorkspacePermission;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

/** Read-only account application API for authorizing a permission within one workspace scope. */
public interface WorkspacePermissionAuthorizer {

  void requirePermission(
      @NonNull WorkspaceId workspaceId,
      @NonNull WorkspacePermission permission
  );
}
