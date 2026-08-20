package org.craft.mcs.cap.core.traceability.application.usecase;

import java.util.Map;
import org.craft.mcs.cap.core.traceability.application.contract.MaterialStateLineage;
import org.craft.mcs.cap.core.traceability.application.port.in.QueryMaterialStateLineageUseCase;
import org.craft.mcs.cap.core.traceability.application.port.out.BatchTraceabilityAccessControl;
import org.craft.mcs.cap.core.traceability.application.port.out.MaterialStateLineageStore;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Authorizes and returns state ancestry without exposing Traceability persistence. */
public final class QueryMaterialStateLineageService implements QueryMaterialStateLineageUseCase {

  private final @NonNull MaterialStateLineageStore lineageStore;
  private final @NonNull BatchTraceabilityAccessControl accessControl;

  public QueryMaterialStateLineageService(
      @NonNull MaterialStateLineageStore lineageStore,
      @NonNull BatchTraceabilityAccessControl accessControl
  ) {
    this.lineageStore = requireNonNull(lineageStore, "material state lineage store cannot be null");
    this.accessControl = requireNonNull(accessControl, "traceability access control cannot be null");
  }

  @Override
  public @NonNull Map<@NonNull BatchTraceSubject, @NonNull MaterialStateLineage> findLineages(
      @NonNull WorkspaceId workspaceId,
      @NonNull Map<@NonNull BatchTraceSubject, @NonNull MaterialStateId> currentStates
  ) {
    requireNonNull(workspaceId, "lineage workspace id cannot be null");
    requireNonNull(currentStates, "current material states cannot be null");
    accessControl.requireReadAccess(workspaceId);
    if (currentStates.isEmpty()) {
      return Map.of();
    }
    return lineageStore.findLineages(workspaceId, Map.copyOf(currentStates));
  }
}
