package org.craft.mcs.cap.core.measurement.application.model;

import java.time.Instant;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRetractionReason;
import org.craft.mcs.cap.shared.kernel.identity.ActorReference;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Read model of the audit evidence retained when a complete measurement set is retracted. */
public record MeasurementSetRetractionView(
    @NonNull ActorReference actor,
    @NonNull Instant retractedAt,
    @NonNull MeasurementRetractionReason reason
) {

  public MeasurementSetRetractionView {
    requireNonNull(actor, "measurement-set retraction actor cannot be null");
    requireNonNull(retractedAt, "measurement-set retraction time cannot be null");
    requireNonNull(reason, "measurement-set retraction reason cannot be null");
  }
}
