package org.craft.mcs.cap.core.production.application.port.out;

import org.craft.mcs.cap.shared.application.ApplicationValidationException;

import java.util.UUID;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Production trace source for material consumed directly from a feedstock batch.
 */
public record ProductionBatchFeedstockTraceSource(
    @NonNull FeedstockBatchId feedstockBatchId,
    @NonNull MaterialStateId materialStateId,
    @Nullable ProductionSourceDisplayName displayName,
    @NonNull VolumeValue volume,
    @Nullable ChemicalProfileAxes profile,
    @Nullable ConfidenceLevel confidence
) implements ProductionBatchTraceSource {

  public ProductionBatchFeedstockTraceSource {
    requireNonNull(feedstockBatchId, "feedstock batch id cannot be null");
    requireNonNull(materialStateId, "feedstock material state id cannot be null");
    requireNonNull(volume, "production trace source volume cannot be null");

    if (volume.isZero()) {
      throw new ApplicationValidationException("production trace source volume must be positive");
    }
  }

  @Override
  public @NonNull UUID id() {
    return feedstockBatchId.value();
  }
}
