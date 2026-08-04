package org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.objective;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.InvalidCompositionOptimizationException;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isNegative;

/**
 * Explainable penalty added to the optimization objective score.
 */
public record CompositionOptimizationPenalty(
    @NonNull CompositionOptimizationPenaltyCode code,
    @NonNull CompositionOptimizationPenaltySeverity severity,
    @NonNull BigDecimal value,
    @Nullable MeasurementCharacteristic characteristic,
    @Nullable BigDecimal actualValue,
    @Nullable BigDecimal threshold
) {

  public CompositionOptimizationPenalty {
    requireNonNull(code, "composition optimization penalty code cannot be null");
    requireNonNull(severity, "composition optimization penalty severity cannot be null");
    requireNonNull(value, "composition optimization penalty value cannot be null");
    if (isNegative(value)) {
      throw new InvalidCompositionOptimizationException(
          "composition optimization penalty cannot be negative"
      );
    }
  }
}
