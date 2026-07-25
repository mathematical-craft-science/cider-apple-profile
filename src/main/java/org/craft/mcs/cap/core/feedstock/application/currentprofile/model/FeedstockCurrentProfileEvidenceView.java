package org.craft.mcs.cap.core.feedstock.application.currentprofile.model;

import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceValidity;
import org.craft.mcs.cap.shared.application.measurement.MeasurementFreshnessStatus;
import org.craft.mcs.cap.shared.application.measurement.MeasurementValueOrigin;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Evidence and freshness selected independently for one current Feedstock profile characteristic. */
public record FeedstockCurrentProfileEvidenceView(
    @NonNull MeasurementCharacteristic characteristic,
    @NonNull MeasurementValueOrigin origin,
    @NonNull MaterialStateId materialStateId,
    @NonNull UUID measurementSetId,
    @NonNull UUID recordId,
    @NonNull Instant measuredAt,
    @Nullable String source,
    @Nullable ConfidenceLevel confidence,
    @NonNull MeasurementEvidenceValidity validity,
    @NonNull MeasurementFreshnessStatus freshnessStatus,
    boolean stale
) {

  public FeedstockCurrentProfileEvidenceView {
    requireNonNull(characteristic, "feedstock profile characteristic cannot be null");
    requireNonNull(origin, "feedstock measurement value origin cannot be null");
    requireNonNull(materialStateId, "feedstock measurement material state id cannot be null");
    requireNonNull(measurementSetId, "measurement set id cannot be null");
    requireNonNull(recordId, "measurement record id cannot be null");
    requireNonNull(measuredAt, "measurement evidence time cannot be null");
    requireNonNull(validity, "measurement evidence validity cannot be null");
    requireNonNull(freshnessStatus, "measurement freshness status cannot be null");
  }
}
