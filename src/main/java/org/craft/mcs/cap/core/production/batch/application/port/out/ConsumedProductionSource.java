package org.craft.mcs.cap.core.production.batch.application.port.out;

import org.craft.mcs.cap.core.production.batch.domain.model.value.ProductionSourceCharacteristics;
import org.craft.mcs.cap.core.production.batch.domain.model.value.ProductionSourceDisplayName;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceEvidenceReference;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.material.value.MaterialConsumption;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Immutable snapshot of material consumed from a production source.
 *
 * <p>The production use case records this source fact without depending on the source context's
 * aggregate or persistence model. The shared consumption fact preserves volume conservation and
 * identifies the source states before and after consumption.
 */
public record ConsumedProductionSource(
    @NonNull ProductionSourceReference reference,
    @Nullable ProductionSourceDisplayName displayName,
    @NonNull MaterialConsumption consumption,
    @NonNull ProductionSourceCharacteristics characteristics,
    @Nullable ConfidenceLevel confidence,
    @NonNull ProductionSourceEvidenceReference evidenceReference
) {

  public ConsumedProductionSource {
    requireNonNull(reference, "production source reference cannot be null");
    requireNonNull(consumption, "production source material consumption cannot be null");
    requireNonNull(characteristics, "consumed production source characteristics cannot be null");
    requireNonNull(evidenceReference, "production source evidence reference cannot be null");
  }

  public @NonNull VolumeValue availableBefore() {
    return consumption.availableBefore();
  }

  public @NonNull VolumeValue consumed() {
    return consumption.consumed();
  }

  public @NonNull VolumeValue remaining() {
    return consumption.remaining();
  }

  public @NonNull MaterialStateId sourceStateId() {
    return consumption.sourceStateId();
  }

  public @NonNull MaterialStateId remainingSourceStateId() {
    return consumption.remainingStateId();
  }
}
