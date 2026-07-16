package org.craft.mcs.cap.core.production.application.model;

import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchKind;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchName;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchStatus;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
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
    @NonNull ProductionBatchName name,
    @NonNull ProductionBatchKind kind,
    @NonNull ProductionBatchStatus status,
    @NonNull VolumeValue volume,
    @NonNull ProductionProfileSnapshot profile,
    @Nullable String note
) {

  public ProductionBatchView {
    requireNonNull(batchId, "production batch id cannot be null");
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(name, "production batch name cannot be null");
    requireNonNull(kind, "production batch kind cannot be null");
    requireNonNull(status, "production batch status cannot be null");
    requireNonNull(volume, "production volume cannot be null");
    requireNonNull(profile, "production profile cannot be null");
  }
}
