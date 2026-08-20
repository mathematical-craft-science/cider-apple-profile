package org.craft.mcs.cap.core.cideranalysis.readiness.domain;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record FermentationStartReadinessCheck(
    @NonNull FermentationStartReadinessFactor factor,
    @NonNull FermentationStartReadinessSeverity severity,
    @NonNull FermentationStartReadinessCheckCode code,
    @NonNull String message,
    @Nullable BigDecimal value,
    @Nullable String unit,
    @Nullable FermentationStartPreparationActionType managedByAction
) {

  public FermentationStartReadinessCheck(
      @NonNull FermentationStartReadinessFactor factor,
      @NonNull FermentationStartReadinessSeverity severity,
      @NonNull FermentationStartReadinessCheckCode code,
      @NonNull String message,
      @Nullable BigDecimal value,
      @Nullable String unit
  ) {
    this(factor, severity, code, message, value, unit, null);
  }

  public FermentationStartReadinessCheck {
    requireNonNull(factor, "fermentation start readiness factor cannot be null");
    requireNonNull(severity, "fermentation start readiness severity cannot be null");
    requireNonNull(code, "fermentation start readiness check code cannot be null");
    requireNonNull(message, "fermentation start readiness check message cannot be null");
    if (message.isBlank()) {
      throw new DomainRuleViolationException("fermentation start readiness check message cannot be blank");
    }
    if (value != null && unit == null) {
      throw new DomainRuleViolationException("fermentation start readiness value requires a unit");
    }
    if (unit != null && unit.isBlank()) {
      throw new DomainRuleViolationException("fermentation start readiness unit cannot be blank");
    }
    if (factor != factorOf(code)) {
      throw new DomainRuleViolationException("fermentation start readiness code must match its factor");
    }
    if (isManaged(code) != (managedByAction != null)) {
      throw new DomainRuleViolationException(
          "managed fermentation start readiness code must match its preparation action"
      );
    }
    if (managedByAction != null && !isCompatibleAction(code, managedByAction)) {
      throw new DomainRuleViolationException(
          "fermentation start readiness preparation action must manage the assessed factor"
      );
    }
  }

  private static @NonNull FermentationStartReadinessFactor factorOf(
      @NonNull FermentationStartReadinessCheckCode code
  ) {
    return switch (code) {
      case SPECIFIC_GRAVITY_MISSING, SPECIFIC_GRAVITY_PRESENT ->
          FermentationStartReadinessFactor.SPECIFIC_GRAVITY;
      case PH_MISSING,
          PH_BELOW_GENERIC_YEAST_TOLERANCE,
          PH_LOW,
          PH_VERY_HIGH_MICROBIAL_RISK,
          PH_VERY_HIGH_MICROBIAL_RISK_MANAGED,
          PH_HIGH_MICROBIAL_RISK,
          PH_HIGH_MICROBIAL_RISK_MANAGED,
          PH_ACCEPTABLE -> FermentationStartReadinessFactor.PH;
      case YAN_MISSING,
          YAN_SEVERELY_LOW,
          YAN_SEVERELY_LOW_MANAGED,
          YAN_LOW,
          YAN_LOW_MANAGED,
          YAN_HIGH,
          YAN_ACCEPTABLE -> FermentationStartReadinessFactor.YEAST_ASSIMILABLE_NITROGEN;
    };
  }

  private static boolean isManaged(@NonNull FermentationStartReadinessCheckCode code) {
    return switch (code) {
      case PH_VERY_HIGH_MICROBIAL_RISK_MANAGED,
          PH_HIGH_MICROBIAL_RISK_MANAGED,
          YAN_SEVERELY_LOW_MANAGED,
          YAN_LOW_MANAGED -> true;
      case SPECIFIC_GRAVITY_MISSING,
          SPECIFIC_GRAVITY_PRESENT,
          PH_MISSING,
          PH_BELOW_GENERIC_YEAST_TOLERANCE,
          PH_LOW,
          PH_VERY_HIGH_MICROBIAL_RISK,
          PH_HIGH_MICROBIAL_RISK,
          PH_ACCEPTABLE,
          YAN_MISSING,
          YAN_SEVERELY_LOW,
          YAN_LOW,
          YAN_HIGH,
          YAN_ACCEPTABLE -> false;
    };
  }

  private static boolean isCompatibleAction(
      @NonNull FermentationStartReadinessCheckCode code,
      @NonNull FermentationStartPreparationActionType action
  ) {
    return switch (code) {
      case PH_VERY_HIGH_MICROBIAL_RISK_MANAGED, PH_HIGH_MICROBIAL_RISK_MANAGED -> switch (action) {
        case SO2_ADDITION_PLANNED,
            SO2_ADDITION_CONFIRMED,
            ACID_ADJUSTMENT_PLANNED,
            ACID_ADJUSTMENT_CONFIRMED -> true;
        default -> false;
      };
      case YAN_SEVERELY_LOW_MANAGED, YAN_LOW_MANAGED -> switch (action) {
        case YEAST_NUTRIENT_ADDITION_PLANNED, YEAST_NUTRIENT_ADDITION_CONFIRMED -> true;
        default -> false;
      };
      default -> false;
    };
  }
}
