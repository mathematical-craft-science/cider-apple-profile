package org.craft.mcs.cap.infrastructure.adapter.out.authorization.account;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.context.application.port.in.WorkspacePermissionAuthorizer;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityAccessControl;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.core.account.membership.domain.model.WorkspacePermission.FEEDSTOCK_WRITE;
import static org.craft.mcs.cap.core.account.membership.domain.model.WorkspacePermission.PRODUCTION_READ;
import static org.craft.mcs.cap.core.account.membership.domain.model.WorkspacePermission.PRODUCTION_WRITE;


/** Translates traceability access into the corresponding CAP workspace permissions. */
@Component
@RequiredArgsConstructor
public final class AccountBatchTraceabilityAccessControl implements BatchTraceabilityAccessControl {

  private final @NonNull WorkspacePermissionAuthorizer workspacePermissionAuthorizer;

  @Override
  public void requireReadAccess(@NonNull WorkspaceId workspaceId) {
    workspacePermissionAuthorizer.requirePermission(workspaceId, PRODUCTION_READ);
  }

  @Override
  public void requireWriteAccess(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull BatchTraceSubjectType> subjectTypes
  ) {
    requireNonNull(subjectTypes, "batch traceability subject types cannot be null")
        .stream()
        .sorted()
        .forEach(subjectType -> workspacePermissionAuthorizer.requirePermission(
            workspaceId,
            switch (subjectType) {
              case FEEDSTOCK_BATCH -> FEEDSTOCK_WRITE;
              case PRODUCTION_BATCH -> PRODUCTION_WRITE;
            }
        ));
  }
}
