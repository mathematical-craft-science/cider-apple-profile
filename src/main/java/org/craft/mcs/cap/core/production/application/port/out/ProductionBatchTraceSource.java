package org.craft.mcs.cap.core.production.application.port.out;

import java.util.UUID;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/**
 * Source snapshot used by production to record traceability facts.
 *
 * <p>The sealed hierarchy keeps production language explicit without mirroring traceability's
 * subject enum. The infrastructure adapter chooses how each production source type is represented
 * in Batch Traceability. {@code sourceStateId} and {@code availableBefore} describe the input
 * state; {@code remainingSourceStateId} and {@code remaining} describe its output state.
 */
public sealed interface ProductionBatchTraceSource
    permits ProductionBatchFeedstockTraceSource {

  @NonNull UUID id();

  @NonNull MaterialStateId sourceStateId();

  @NonNull MaterialStateId remainingSourceStateId();

  @Nullable ProductionSourceDisplayName displayName();

  @NonNull VolumeValue availableBefore();

  @NonNull VolumeValue consumed();

  @NonNull VolumeValue remaining();

  @Nullable ChemicalProfileAxes profile();

  @Nullable ConfidenceLevel confidence();
}
