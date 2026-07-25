package org.craft.mcs.cap.core.feedstock.application.inventory.port.in;

import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Immutable current feedstock evidence returned for production candidate assessment. */
public record FeedstockBatchMaterialSnapshot(
    @NonNull FeedstockBatchId batchId,
    @NonNull FeedstockBatchMaterialName batchName,
    @NonNull VolumeValue requestedVolume,
    @NonNull VolumeValue availableVolume,
    @NonNull MaterialStateId materialStateId,
    @NonNull FeedstockBatchMaterialEvidenceReference evidenceReference,
    @NonNull ChemicalProfile chemicalProfile,
    @Nullable ConfidenceLevel profileConfidence
) {

  public FeedstockBatchMaterialSnapshot {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(batchName, "feedstock batch name cannot be null");
    requireNonNull(requestedVolume, "requested feedstock volume cannot be null");
    requireNonNull(availableVolume, "available feedstock volume cannot be null");
    requireNonNull(materialStateId, "feedstock material state id cannot be null");
    requireNonNull(evidenceReference, "feedstock material evidence reference cannot be null");
    requireNonNull(chemicalProfile, "feedstock chemical profile cannot be null");
  }
}
