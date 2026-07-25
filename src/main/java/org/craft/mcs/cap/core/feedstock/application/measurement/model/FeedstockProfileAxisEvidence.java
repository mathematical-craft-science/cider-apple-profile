package org.craft.mcs.cap.core.feedstock.application.measurement.model;

import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceValidity;
import org.craft.mcs.cap.shared.application.measurement.MeasurementValueOrigin;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Per-axis evidence selected for the current Feedstock chemical profile projection. */
public record FeedstockProfileAxisEvidence(
    @NonNull MeasurementCharacteristic characteristic,
    @NonNull MeasurementValueOrigin origin,
    @NonNull MaterialStateId materialStateId,
    @NonNull UUID measurementSetId,
    @NonNull UUID recordId,
    @NonNull Instant measuredAt,
    @Nullable String source,
    @Nullable ConfidenceLevel confidence,
    @NonNull MeasurementEvidenceValidity validity
) {

  public FeedstockProfileAxisEvidence {
    requireNonNull(characteristic, "chemical characteristic cannot be null");
    requireNonNull(origin, "measurement value origin cannot be null");
    requireNonNull(materialStateId, "measurement material state id cannot be null");
    requireNonNull(measurementSetId, "measurement set id cannot be null");
    requireNonNull(recordId, "measurement record id cannot be null");
    requireNonNull(measuredAt, "measurement evidence time cannot be null");
    requireNonNull(validity, "measurement evidence validity cannot be null");
  }
}
