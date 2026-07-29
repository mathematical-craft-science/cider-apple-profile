package org.craft.mcs.cap.shared.application.measurement;

import java.time.Instant;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Policy-neutral snapshot of the immutable evidence selected for one current material characteristic.
 *
 * <p>Freshness is deliberately absent because it depends on the workspace policy and assessment time.
 * Application contexts can enrich this snapshot into a {@link MeasurementEvidenceView}.</p>
 */
public record MeasurementEvidenceSnapshot(
    @NonNull MeasurementCharacteristic characteristic,
    @NonNull MeasurementValueOrigin origin,
    @NonNull MaterialStateId materialStateId,
    @NonNull MeasurementEvidenceReference evidenceReference,
    @NonNull Instant measuredAt,
    @NonNull MeasurementProvenanceView provenance
) {

  public MeasurementEvidenceSnapshot {
    requireNonNull(characteristic, "measurement evidence characteristic cannot be null");
    requireNonNull(origin, "measurement evidence origin cannot be null");
    requireNonNull(materialStateId, "measurement evidence material state id cannot be null");
    requireNonNull(evidenceReference, "measurement evidence reference cannot be null");
    requireNonNull(measuredAt, "measurement evidence time cannot be null");
    requireNonNull(provenance, "measurement evidence provenance cannot be null");
  }
}
