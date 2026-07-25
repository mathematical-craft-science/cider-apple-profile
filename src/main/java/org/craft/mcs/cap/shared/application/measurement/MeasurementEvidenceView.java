package org.craft.mcs.cap.shared.application.measurement;

import java.time.Instant;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Published evidence selected for one characteristic of a current material profile. */
public record MeasurementEvidenceView(
    @NonNull MeasurementCharacteristic characteristic,
    @NonNull MeasurementValueOrigin origin,
    @NonNull MaterialStateId materialStateId,
    @NonNull MeasurementEvidenceReference evidenceReference,
    @NonNull Instant measuredAt,
    @NonNull MeasurementProvenanceView provenance,
    @NonNull MeasurementFreshnessStatus freshnessStatus,
    @NonNull MeasurementEvidenceValidity validity
) {

  public MeasurementEvidenceView {
    requireNonNull(characteristic, "measurement evidence characteristic cannot be null");
    requireNonNull(origin, "measurement evidence origin cannot be null");
    requireNonNull(materialStateId, "measurement evidence material state id cannot be null");
    requireNonNull(evidenceReference, "measurement evidence reference cannot be null");
    requireNonNull(measuredAt, "measurement evidence time cannot be null");
    requireNonNull(provenance, "measurement evidence provenance cannot be null");
    requireNonNull(freshnessStatus, "measurement evidence freshness cannot be null");
    requireNonNull(validity, "measurement evidence validity cannot be null");
  }
}
