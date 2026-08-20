package org.craft.mcs.cap.core.production.planning.application.confirmation.contract;

import org.craft.mcs.cap.core.production.batch.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.batch.domain.model.value.AlcoholicFermentationState;
import org.craft.mcs.cap.shared.application.material.VesselReferenceView;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchLifecycle;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/** Planning-owned acknowledgement of a successfully materialized production candidate. */
public record ProductionBatchConfirmationResult(
    @NonNull ProductionBatchId batchId,
    @NonNull WorkspaceId workspaceId,
    @NonNull BatchName name,
    @NonNull VesselReferenceView vessel,
    @NonNull BatchLifecycle lifecycle,
    @NonNull AlcoholicFermentationState alcoholicFermentationState,
    @Nullable String note
) {

  public ProductionBatchConfirmationResult {
    requireNonNull(batchId, "production batch id cannot be null");
    requireNonNull(workspaceId, "production workspace id cannot be null");
    requireNonNull(name, "production batch name cannot be null");
    requireNonNull(vessel, "production vessel cannot be null");
    requireNonNull(lifecycle, "production batch lifecycle cannot be null");
    requireNonNull(alcoholicFermentationState, "alcoholic fermentation state cannot be null");
  }
}
