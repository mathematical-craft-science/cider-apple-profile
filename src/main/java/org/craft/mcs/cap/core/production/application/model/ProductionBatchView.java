package org.craft.mcs.cap.core.production.application.model;

import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.domain.model.value.AlcoholicFermentationState;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchLifecycle;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.shared.kernel.material.value.Vessel;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * Immutable application read model of current production batch state.
 *
 * <p>Inbound adapters receive this projection instead of the mutable aggregate root.
 */
public record ProductionBatchView(
    @NonNull ProductionBatchId batchId,
    @NonNull WorkspaceId workspaceId,
    @NonNull BatchName name,
    @NonNull Vessel vessel,
    @NonNull ProductionBatchLifecycle lifecycle,
    @NonNull AlcoholicFermentationState alcoholicFermentationState,
    @NonNull VolumeValue currentVolume,
    @NonNull ProductionProfileSnapshot initialProfileSnapshot,
    @Nullable String note
) {

  public ProductionBatchView {
    requireNonNull(batchId, "production batch id cannot be null");
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(name, "production batch name cannot be null");
    requireNonNull(vessel, "production vessel cannot be null");
    requireNonNull(lifecycle, "production batch lifecycle cannot be null");
    requireNonNull(alcoholicFermentationState, "alcoholic fermentation state cannot be null");
    requireNonNull(currentVolume, "production current volume cannot be null");
    requireNonNull(initialProfileSnapshot, "production initial profile snapshot cannot be null");
  }
}
