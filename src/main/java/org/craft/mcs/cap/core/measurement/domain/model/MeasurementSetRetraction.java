package org.craft.mcs.cap.core.measurement.domain.model;

import java.time.Instant;
import org.craft.mcs.cap.shared.kernel.identity.ActorReference;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Audit evidence identifying who retracted a complete measurement set, when, and why. */
@ValueObject
public record MeasurementSetRetraction(
    @NonNull ActorReference actor,
    @NonNull Instant retractedAt,
    @NonNull MeasurementRetractionReason reason
) {

  public MeasurementSetRetraction {
    requireNonNull(actor, "measurement-set retraction actor cannot be null");
    requireNonNull(retractedAt, "measurement-set retraction time cannot be null");
    requireNonNull(reason, "measurement-set retraction reason cannot be null");
  }
}
