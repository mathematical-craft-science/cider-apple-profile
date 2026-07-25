package org.craft.mcs.cap.core.production.application.port.out;

import java.util.UUID;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
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
    @NonNull VolumeValue availableBefore,
    @NonNull VolumeValue consumed,
    @NonNull VolumeValue remaining,
    @Nullable ChemicalProfileAxes profile,
    @Nullable ConfidenceLevel confidence
) implements ProductionBatchTraceSource {

  public ProductionBatchFeedstockTraceSource {
    requireNonNull(feedstockBatchId, "feedstock batch id cannot be null");
    requireNonNull(materialStateId, "feedstock material state id cannot be null");
    requireNonNull(availableBefore, "production trace source volume before consumption cannot be null");
    requireNonNull(consumed, "production trace source consumed volume cannot be null");
    requireNonNull(remaining, "production trace source remaining volume cannot be null");

    if (consumed.isZero()) {
      throw new ApplicationValidationException("production trace source volume must be positive");
    }
    if (availableBefore.liters().compareTo(consumed.liters().add(remaining.liters())) != 0) {
      throw new ApplicationValidationException(
          "production trace source volume before consumption must equal consumed plus remaining volume"
      );
    }
  }

  @Override
  public @NonNull UUID id() {
    return feedstockBatchId.value();
  }
}
