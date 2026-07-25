package org.craft.mcs.cap.core.measurement.domain.model;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementSourceType;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Source, method, and confidence evidence retained independently for each record. */
@ValueObject
public record MeasurementProvenance(
    @NonNull MeasurementSourceType sourceType,
    @Nullable MeasurementSourceName sourceName,
    @Nullable MeasurementMethod method,
    @Nullable MeasurementSourceReference reference,
    @Nullable ConfidenceLevel confidence
) {

  public MeasurementProvenance {
    requireNonNull(sourceType, "measurement source type cannot be null");
    if (sourceType == MeasurementSourceType.DERIVED
        && (sourceName != null || method != null || reference != null)) {
      throw new DomainRuleViolationException(
          "derived measurement provenance cannot declare external source metadata"
      );
    }
  }

  /** Provenance for a result derived entirely from immutable records in the same revision. */
  public static @NonNull MeasurementProvenance derived() {
    return new MeasurementProvenance(MeasurementSourceType.DERIVED, null, null, null, null);
  }
}
