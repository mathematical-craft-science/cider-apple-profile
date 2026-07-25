package org.craft.mcs.cap.shared.application.measurement;

import java.util.UUID;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Published reference to one immutable result in a logical MeasurementSet. */
public record MeasurementEvidenceReference(
    @NonNull UUID measurementSetId,
    @NonNull UUID recordId
) {

  public MeasurementEvidenceReference {
    requireNonNull(measurementSetId, "measurement evidence set id cannot be null");
    requireNonNull(recordId, "measurement evidence record id cannot be null");
  }
}
