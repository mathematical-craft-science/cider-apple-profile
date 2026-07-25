package org.craft.mcs.cap.shared.application.measurement;

import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementSourceType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Published read projection of provenance attached to immutable measurement evidence.
 *
 * <p>Optional text values are opaque display metadata already validated by the Measurement context.</p>
 */
public record MeasurementProvenanceView(
    @NonNull MeasurementSourceType sourceType,
    @Nullable String sourceName,
    @Nullable String method,
    @Nullable String reference,
    @Nullable ConfidenceLevel confidence
) {

  public MeasurementProvenanceView {
    requireNonNull(sourceType, "measurement source type cannot be null");
  }
}
