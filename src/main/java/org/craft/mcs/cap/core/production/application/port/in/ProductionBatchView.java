package org.craft.mcs.cap.core.production.application.port.in;

import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchKind;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchName;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchStatus;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionTargetDistance;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Application read model for production batch list and detail queries.
 *
 * <p>The view mirrors persisted production state needed by API read scenarios without exposing the
 * mutable aggregate root as the result of query use cases.
 */
public record ProductionBatchView(
    @NonNull ProductionBatchId batchId,
    @NonNull CiderMakerId ciderMakerId,
    @NonNull ProductionBatchName name,
    @NonNull ProductionBatchKind kind,
    @NonNull ProductionBatchStatus status,
    @NonNull VolumeValue volume,
    @NonNull ProductionProfileSnapshot profile,
    @Nullable ProductionTargetDistance targetDistance,
    @Nullable String note
) {

  public ProductionBatchView {
    requireNonNull(batchId, "production batch id cannot be null");
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
    requireNonNull(name, "production batch name cannot be null");
    requireNonNull(kind, "production batch kind cannot be null");
    requireNonNull(status, "production batch status cannot be null");
    requireNonNull(volume, "production volume cannot be null");
    requireNonNull(profile, "production profile cannot be null");
  }
}
