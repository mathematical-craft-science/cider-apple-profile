package org.craft.mcs.cap.infrastructure.adapter.out.authorization.account;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.context.application.port.in.WorkspacePermissionAuthorizer;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityAccessControl;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static org.craft.mcs.cap.core.account.membership.domain.model.WorkspacePermission.PRODUCTION_READ;


/** Translates traceability read access into the corresponding CAP workspace permission. */
@Component
@RequiredArgsConstructor
public final class AccountBatchTraceabilityAccessControl implements BatchTraceabilityAccessControl {

  private final @NonNull WorkspacePermissionAuthorizer workspacePermissionAuthorizer;

  @Override
  public void requireReadAccess(@NonNull WorkspaceId workspaceId) {
    workspacePermissionAuthorizer.requirePermission(workspaceId, PRODUCTION_READ);
  }
}
