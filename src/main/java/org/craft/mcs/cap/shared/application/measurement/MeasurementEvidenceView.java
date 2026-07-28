package org.craft.mcs.cap.shared.application.measurement;

import java.time.Instant;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Freshness-aware application view over one policy-neutral evidence snapshot.
 *
 * <p>The snapshot remains the single representation of evidence identity and provenance. This
 * higher-level view adds only the policy result that depends on assessment time and workspace
 * settings.</p>
 */
public record MeasurementEvidenceView(
    @NonNull MeasurementEvidenceSnapshot snapshot,
    @NonNull MeasurementFreshnessStatus freshnessStatus
) {

  public MeasurementEvidenceView {
    requireNonNull(snapshot, "measurement evidence snapshot cannot be null");
    requireNonNull(freshnessStatus, "measurement evidence freshness cannot be null");
  }

  public MeasurementEvidenceView(
      @NonNull MeasurementCharacteristic characteristic,
      @NonNull MeasurementValueOrigin origin,
      @NonNull MaterialStateId materialStateId,
      @NonNull MeasurementEvidenceReference evidenceReference,
      @NonNull Instant measuredAt,
      @NonNull MeasurementProvenanceView provenance,
      @NonNull MeasurementFreshnessStatus freshnessStatus,
      @NonNull MeasurementEvidenceValidity validity
  ) {
    this(
        new MeasurementEvidenceSnapshot(
            characteristic,
            origin,
            materialStateId,
            evidenceReference,
            measuredAt,
            provenance,
            validity
        ),
        freshnessStatus
    );
  }

  public @NonNull MeasurementCharacteristic characteristic() {
    return snapshot.characteristic();
  }

  public @NonNull MeasurementValueOrigin origin() {
    return snapshot.origin();
  }

  public @NonNull MaterialStateId materialStateId() {
    return snapshot.materialStateId();
  }

  public @NonNull MeasurementEvidenceReference evidenceReference() {
    return snapshot.evidenceReference();
  }

  public @NonNull Instant measuredAt() {
    return snapshot.measuredAt();
  }

  public @NonNull MeasurementProvenanceView provenance() {
    return snapshot.provenance();
  }

  public @NonNull MeasurementEvidenceValidity validity() {
    return snapshot.validity();
  }
}
