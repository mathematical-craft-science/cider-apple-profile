package org.craft.mcs.cap.core.feedstock.application.inventory.port.in;

import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.shared.kernel.material.value.MaterialAvailability;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Immutable current feedstock evidence returned for production candidate assessment. */
public record FeedstockBatchMaterialSnapshot(
    @NonNull FeedstockBatchId batchId,
    @NonNull BatchName batchName,
    @NonNull MaterialAvailability availability,
    @NonNull FeedstockBatchMaterialEvidenceReference evidenceReference,
    @NonNull FeedstockMaterialCharacteristics characteristics,
    @Nullable ConfidenceLevel profileConfidence
) {

  public FeedstockBatchMaterialSnapshot {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(batchName, "feedstock batch name cannot be null");
    requireNonNull(availability, "feedstock material availability cannot be null");
    requireNonNull(evidenceReference, "feedstock material evidence reference cannot be null");
    requireNonNull(characteristics, "feedstock material characteristics cannot be null");
  }

  public @NonNull VolumeValue requestedVolume() {
    return availability.requestedVolume();
  }

  public @NonNull VolumeValue availableVolume() {
    return availability.availableVolume();
  }

  public @NonNull MaterialStateId materialStateId() {
    return availability.materialStateId();
  }
}
