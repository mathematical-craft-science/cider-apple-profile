package org.craft.mcs.cap.core.account.application.port.in;

import org.craft.mcs.cap.core.account.domain.model.AccountPermission;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.jspecify.annotations.NonNull;


/** Read-only account application API for authorizing a permission within one workspace scope. */
public interface WorkspacePermissionAuthorizer {

  void requirePermission(
      @NonNull CiderMakerId ciderMakerId,
      @NonNull AccountPermission permission
  );
}
