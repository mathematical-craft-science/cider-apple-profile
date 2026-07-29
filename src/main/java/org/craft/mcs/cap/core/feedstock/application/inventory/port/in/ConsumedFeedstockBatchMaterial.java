package org.craft.mcs.cap.core.feedstock.application.inventory.port.in;

import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.shared.kernel.material.value.MaterialConsumption;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Immutable feedstock-owned snapshot returned with a successful material consumption.
 *
 * <p>The profile captures the current evidence used when the volume left the feedstock batch.
 * The shared consumption fact identifies the source before and after consumption. Consumers must
 * not query the mutable batch again to reconstruct this fact.
 */
public record ConsumedFeedstockBatchMaterial(
    @NonNull FeedstockBatchId batchId,
    @NonNull BatchName batchName,
    @NonNull MaterialConsumption consumption,
    @NonNull FeedstockBatchMaterialEvidenceReference evidenceReference,
    @NonNull FeedstockMaterialCharacteristics characteristics,
    @Nullable ConfidenceLevel profileConfidence
) {

  public ConsumedFeedstockBatchMaterial {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(batchName, "feedstock batch name cannot be null");
    requireNonNull(consumption, "feedstock material consumption cannot be null");
    requireNonNull(evidenceReference, "feedstock material evidence reference cannot be null");
    requireNonNull(characteristics, "feedstock material characteristics cannot be null");
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

  public @NonNull MaterialStateId remainingStateId() {
    return consumption.remainingStateId();
  }
}
