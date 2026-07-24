package org.craft.mcs.cap.core.feedstock.application.currentprofile.model;

import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockProfileCharacteristic;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Evidence and freshness selected independently for one current Feedstock profile characteristic. */
public record FeedstockCurrentProfileEvidenceView(
    @NonNull FeedstockProfileCharacteristic characteristic,
    @NonNull UUID measurementSetId,
    @NonNull UUID recordId,
    @NonNull Instant measuredAt,
    @Nullable String source,
    @Nullable ConfidenceLevel confidence,
    @NonNull FeedstockCurrentMeasurementFreshnessStatus freshnessStatus,
    boolean stale
) {

  public FeedstockCurrentProfileEvidenceView {
    requireNonNull(characteristic, "feedstock profile characteristic cannot be null");
    requireNonNull(measurementSetId, "measurement set id cannot be null");
    requireNonNull(recordId, "measurement record id cannot be null");
    requireNonNull(measuredAt, "measurement evidence time cannot be null");
    requireNonNull(freshnessStatus, "measurement freshness status cannot be null");
  }
}
