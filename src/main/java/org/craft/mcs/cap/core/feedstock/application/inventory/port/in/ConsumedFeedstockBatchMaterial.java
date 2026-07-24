package org.craft.mcs.cap.core.feedstock.application.inventory.port.in;

import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Immutable feedstock-owned snapshot returned with a successful material consumption.
 *
 * <p>The profile captures the current evidence used when the volume left the feedstock batch.
 * Consumers must not query the mutable batch again to reconstruct this fact.
 */
public record ConsumedFeedstockBatchMaterial(
    @NonNull FeedstockBatchId batchId,
    @NonNull FeedstockBatchMaterialName batchName,
    @NonNull VolumeValue volume,
    @NonNull FeedstockBatchMaterialEvidenceReference evidenceReference,
    @NonNull ChemicalProfile chemicalProfile,
    @Nullable ConfidenceLevel profileConfidence
) {

  public ConsumedFeedstockBatchMaterial {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(batchName, "feedstock batch name cannot be null");
    requireNonNull(volume, "consumed feedstock material volume cannot be null");
    requireNonNull(evidenceReference, "feedstock material evidence reference cannot be null");
    requireNonNull(chemicalProfile, "feedstock chemical profile cannot be null");
  }
}
