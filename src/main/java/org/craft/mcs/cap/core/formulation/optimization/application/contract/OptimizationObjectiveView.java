package org.craft.mcs.cap.core.formulation.optimization.application.contract;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isNegative;


/** Explainable score and readiness penalties used to rank one formulation candidate. */
public record OptimizationObjectiveView(
    @Nullable BigDecimal score,
    @NonNull List<@NonNull Penalty> penalties
) {

  public OptimizationObjectiveView {
    penalties = List.copyOf(requireNonNull(penalties, "optimization penalties cannot be null"));
    if (score != null && isNegative(score)) {
      throw new ApplicationValidationException("optimization score cannot be negative");
    }
  }

  public record Penalty(
      @NonNull Code code,
      @NonNull Severity severity,
      @NonNull BigDecimal value,
      @Nullable MeasurementCharacteristic characteristic,
      @Nullable BigDecimal actualValue,
      @Nullable BigDecimal threshold
  ) {

    public Penalty {
      requireNonNull(code, "optimization penalty code cannot be null");
      requireNonNull(severity, "optimization penalty severity cannot be null");
      requireNonNull(value, "optimization penalty value cannot be null");
      if (isNegative(value)) {
        throw new ApplicationValidationException("optimization penalty cannot be negative");
      }
    }
  }

  public enum Code {
    MISSING_YEAST_ASSIMILABLE_NITROGEN,
    LOW_YEAST_ASSIMILABLE_NITROGEN,
    HIGH_YEAST_ASSIMILABLE_NITROGEN
  }

  public enum Severity { INFO, WARNING, BLOCKING }
}
