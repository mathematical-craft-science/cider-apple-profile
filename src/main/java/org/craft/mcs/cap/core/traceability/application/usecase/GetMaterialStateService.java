package org.craft.mcs.cap.core.traceability.application.usecase;

import org.craft.mcs.cap.core.traceability.application.MaterialStateNotFoundException;
import org.craft.mcs.cap.core.traceability.application.model.MaterialStateSnapshot;
import org.craft.mcs.cap.core.traceability.application.port.in.GetMaterialStateUseCase;
import org.craft.mcs.cap.core.traceability.application.port.out.BatchTraceabilityAccessControl;
import org.craft.mcs.cap.core.traceability.application.port.out.MaterialStateHistoryStore;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Reads an immutable historical material state and enforces workspace access. */
public final class GetMaterialStateService implements GetMaterialStateUseCase {

  private final @NonNull MaterialStateHistoryStore historyStore;
  private final @NonNull BatchTraceabilityAccessControl accessControl;

  public GetMaterialStateService(
      @NonNull MaterialStateHistoryStore historyStore,
      @NonNull BatchTraceabilityAccessControl accessControl
  ) {
    this.historyStore = requireNonNull(
        historyStore,
        "material state history store cannot be null"
    );
    this.accessControl = requireNonNull(
        accessControl,
        "batch traceability access control cannot be null"
    );
  }

  @Override
  public @NonNull MaterialStateSnapshot get(@NonNull MaterialStateId materialStateId) {
    requireNonNull(materialStateId, "material state id cannot be null");
    final var state = historyStore.find(materialStateId)
        .orElseThrow(() -> new MaterialStateNotFoundException(materialStateId));
    accessControl.requireReadAccess(state.workspaceId());
    return state;
  }
}
