package org.craft.mcs.cap.core.feedstock.batch.application.analysis.readiness.contract;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Feedstock-owned fermentation-start readiness facts. */
public record FeedstockFermentationStartReadinessView(
    @NonNull Status status,
    @NonNull String method,
    @NonNull List<@NonNull Check> checks
) {

  public FeedstockFermentationStartReadinessView {
    requireNonNull(status, "fermentation-start readiness status cannot be null");
    requireNonNull(method, "fermentation-start readiness method cannot be null");
    checks = List.copyOf(requireNonNull(checks, "fermentation-start readiness checks cannot be null"));
    if (method.isBlank()) {
      throw new ApplicationValidationException("fermentation-start readiness method cannot be blank");
    }
  }

  public record Check(
      @NonNull Factor factor,
      @NonNull Severity severity,
      @NonNull Code code,
      @NonNull String message,
      @Nullable BigDecimal value,
      @Nullable String unit,
      @Nullable PreparationAction managedByAction
  ) {

    public Check {
      requireNonNull(factor, "readiness factor cannot be null");
      requireNonNull(severity, "readiness severity cannot be null");
      requireNonNull(code, "readiness code cannot be null");
      requireNonNull(message, "readiness message cannot be null");
      if (message.isBlank()) {
        throw new ApplicationValidationException("readiness message cannot be blank");
      }
      if (value != null && unit == null) {
        throw new ApplicationValidationException("readiness value requires a unit");
      }
      if (unit != null && unit.isBlank()) {
        throw new ApplicationValidationException("readiness unit cannot be blank");
      }
    }
  }

  public enum Status { READY, NEEDS_ATTENTION, NOT_READY }

  public enum Factor { SPECIFIC_GRAVITY, PH, YEAST_ASSIMILABLE_NITROGEN }

  public enum Severity { INFO, WARNING, BLOCKER }

  public enum PreparationAction {
    YEAST_SELECTED,
    SO2_ADDITION_PLANNED,
    SO2_ADDITION_CONFIRMED,
    ACID_ADJUSTMENT_PLANNED,
    ACID_ADJUSTMENT_CONFIRMED,
    PH_REMEASURED,
    YEAST_NUTRIENT_ADDITION_PLANNED,
    YEAST_NUTRIENT_ADDITION_CONFIRMED,
    OXYGENATION_PLANNED,
    OXYGENATION_CONFIRMED,
    YAN_REMEASURED
  }

  public enum Code {
    SPECIFIC_GRAVITY_MISSING,
    SPECIFIC_GRAVITY_PRESENT,
    PH_MISSING,
    PH_BELOW_GENERIC_YEAST_TOLERANCE,
    PH_LOW,
    PH_VERY_HIGH_MICROBIAL_RISK,
    PH_VERY_HIGH_MICROBIAL_RISK_MANAGED,
    PH_HIGH_MICROBIAL_RISK,
    PH_HIGH_MICROBIAL_RISK_MANAGED,
    PH_ACCEPTABLE,
    YAN_MISSING,
    YAN_SEVERELY_LOW,
    YAN_SEVERELY_LOW_MANAGED,
    YAN_LOW,
    YAN_LOW_MANAGED,
    YAN_HIGH,
    YAN_ACCEPTABLE
  }
}
