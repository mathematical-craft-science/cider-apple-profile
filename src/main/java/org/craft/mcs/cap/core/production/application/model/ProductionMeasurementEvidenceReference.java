package org.craft.mcs.cap.core.production.application.model;

import java.util.UUID;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Production-owned reference to the immutable Measurement evidence selected for a characteristic.
 *
 * <p>The reference preserves the identities required for traceability without exposing Measurement
 * domain identity types across the bounded-context boundary.</p>
 */
public record ProductionMeasurementEvidenceReference(
    @NonNull UUID measurementSetId,
    @NonNull UUID recordId
) {

  public ProductionMeasurementEvidenceReference {
    requireNonNull(measurementSetId, "production measurement set id cannot be null");
    requireNonNull(recordId, "production measurement record id cannot be null");
  }
}
