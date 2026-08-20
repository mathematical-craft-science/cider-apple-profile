package org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry;

import java.util.List;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Numerical axis extent and ordered boundaries of one configured SG/TA graph interpretation.
 *
 * <p>The boundary values belong only to that graph interpretation. They are not universal
 * measurement limits or target-distance calibration values.</p>
 */
public record SugarAcidityGraphAxisScale<V extends MeasuredValue>(
    @NonNull V min,
    @NonNull V max,
    @NonNull List<@NonNull V> boundaryValues
) {

  public SugarAcidityGraphAxisScale {
    requireNonNull(min, "graph axis scale minimum cannot be null");
    requireNonNull(max, "graph axis scale maximum cannot be null");
    boundaryValues = List.copyOf(requireNonNull(
        boundaryValues,
        "graph axis scale boundary values cannot be null"
    ));
    if (min.getClass() != max.getClass()) {
      throw new DomainRuleViolationException(
          "graph axis scale bounds must have the same measurement type"
      );
    }
    if (min.compareTo(max) >= 0) {
      throw new DomainRuleViolationException("graph axis scale minimum must be less than maximum");
    }
    validateBoundaryValues(boundaryValues, min, max);
  }

  private static <V extends MeasuredValue> void validateBoundaryValues(
      @NonNull List<@NonNull V> boundaryValues,
      @NonNull V min,
      @NonNull V max
  ) {
    V previous = min;
    for (final var boundaryValue : boundaryValues) {
      if (boundaryValue.getClass() != min.getClass()) {
        throw new DomainRuleViolationException(
            "graph axis scale boundaries must have the same measurement type as its bounds"
        );
      }
      if (previous.compareTo(boundaryValue) >= 0 || boundaryValue.compareTo(max) >= 0) {
        throw new DomainRuleViolationException(
            "graph axis scale boundary values must be strictly increasing inside its bounds"
        );
      }
      previous = boundaryValue;
    }
  }
}
