package org.craft.mcs.cap.core.feedstock.application.measurement.model;

import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Per-axis evidence selected for the current Feedstock chemical profile projection. */
public record FeedstockProfileAxisEvidence(
    @NonNull FeedstockProfileCharacteristic characteristic,
    @NonNull UUID measurementSetId,
    @NonNull UUID recordId,
    @NonNull Instant measuredAt,
    @Nullable String source,
    @Nullable ConfidenceLevel confidence
) {

  public FeedstockProfileAxisEvidence {
    requireNonNull(characteristic, "chemical characteristic cannot be null");
    requireNonNull(measurementSetId, "measurement set id cannot be null");
    requireNonNull(recordId, "measurement record id cannot be null");
    requireNonNull(measuredAt, "measurement evidence time cannot be null");
  }
}
