package org.craft.mcs.cap.core.cideranalysis.readiness.domain.ph;

import java.math.BigDecimal;
import java.util.Set;
import org.craft.mcs.cap.core.cideranalysis.readiness.domain.FermentationStartPreparationActionType;
import org.craft.mcs.cap.core.cideranalysis.readiness.domain.FermentationStartReadinessCheck;
import org.craft.mcs.cap.core.cideranalysis.readiness.domain.FermentationStartReadinessCheckCode;
import org.craft.mcs.cap.core.cideranalysis.readiness.domain.FermentationStartReadinessFactor;
import org.craft.mcs.cap.core.cideranalysis.readiness.domain.FermentationStartReadinessSeverity;
import org.craft.mcs.cap.core.cideranalysis.readiness.domain.ReadinessRuleSource;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.measurement.unit.BeverageUnits;
import org.jmolecules.ddd.annotation.Service;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasurementValueMath.midpoint;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isGreaterThan;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isLessThan;


/**
 * pH readiness policy for the moment immediately before fermentation start.
 *
 * <p>The default v1 thresholds are curated cider business rules. Low pH is treated as possible
 * yeast stress, while high pH is microbial-risk guidance; high pH does not mean yeast cannot start.
 * Future yeast-strain and corrective-action contexts can replace or decorate this policy.
 */
@Service
public record PhFermentationStartPolicy(
    @NonNull BigDecimal genericYeastToleranceMin,
    @NonNull CiderPhGuidanceRange ciderGuidance,
    @NonNull BigDecimal highMicrobialRisk,
    @NonNull ReadinessRuleSource source
) {

  private static final @NonNull BigDecimal DEFAULT_GENERIC_YEAST_TOLERANCE_MIN = new BigDecimal("2.8");
  private static final @NonNull BigDecimal DEFAULT_HIGH_MICROBIAL_RISK = new BigDecimal("4.0");
  private static final @NonNull String PH_UNIT = BeverageUnits.label(BeverageUnits.PH);

  public PhFermentationStartPolicy {
    requireNonNull(genericYeastToleranceMin, "generic yeast pH tolerance minimum cannot be null");
    requireNonNull(ciderGuidance, "cider pH guidance range cannot be null");
    requireNonNull(highMicrobialRisk, "high pH microbial-risk threshold cannot be null");
    requireNonNull(source, "pH fermentation-start policy source cannot be null");
    validateThresholds(genericYeastToleranceMin, ciderGuidance, highMicrobialRisk);
  }

  public static @NonNull PhFermentationStartPolicy curatedCiderV1() {
    return new PhFermentationStartPolicy(
        DEFAULT_GENERIC_YEAST_TOLERANCE_MIN,
        CiderPhGuidanceRange.curatedCiderV1(),
        DEFAULT_HIGH_MICROBIAL_RISK,
        ReadinessRuleSource.curatedPhV1()
    );
  }

  public @NonNull FermentationStartReadinessCheck check(
      @Nullable CharacteristicValue<PhValue> ph
  ) {
    return check(PhFermentationStartContext.withoutCorrectiveActions(ph));
  }

  public @NonNull FermentationStartReadinessCheck check(@NonNull PhFermentationStartContext context) {
    requireNonNull(context, "pH fermentation-start context cannot be null");
    final var ph = context.ph();
    final var value = ph == null ? null : midpoint(ph);
    if (value == null) {
      return check(
          FermentationStartReadinessSeverity.WARNING,
          FermentationStartReadinessCheckCode.PH_MISSING,
          "pH is missing; microbial and yeast-stress risks cannot be assessed.",
          null
      );
    }
    if (isLessThan(value, genericYeastToleranceMin)) {
      return check(
          FermentationStartReadinessSeverity.BLOCKER,
          FermentationStartReadinessCheckCode.PH_BELOW_GENERIC_YEAST_TOLERANCE,
          "pH is below the generic yeast-tolerance guidance; choose a compatible yeast or adjust pH before start.",
          value
      );
    }
    if (ciderGuidance.isBelow(value)) {
      return check(
          FermentationStartReadinessSeverity.WARNING,
          FermentationStartReadinessCheckCode.PH_LOW,
          "pH is low and may increase yeast stress during fermentation start.",
          value
      );
    }
    if (isGreaterThan(value, highMicrobialRisk)) {
      final var action = highPhRiskManagementAction(context.correctiveActions());
      if (action != null) {
        return managedHighPhCheck(
            FermentationStartReadinessCheckCode.PH_VERY_HIGH_MICROBIAL_RISK_MANAGED,
            "pH is high; microbial risk is marked as managed by a planned or confirmed preparation action.",
            value,
            action
        );
      }
      return check(
          FermentationStartReadinessSeverity.WARNING,
          FermentationStartReadinessCheckCode.PH_VERY_HIGH_MICROBIAL_RISK,
          "pH is high; define SO2, acid adjustment, or another microbial-risk strategy before start.",
          value
      );
    }
    if (ciderGuidance.isAbove(value)) {
      final var action = highPhRiskManagementAction(context.correctiveActions());
      if (action != null) {
        return managedHighPhCheck(
            FermentationStartReadinessCheckCode.PH_HIGH_MICROBIAL_RISK_MANAGED,
            "pH is above the curated cider guidance range; microbial risk is marked as managed.",
            value,
            action
        );
      }
      return check(
          FermentationStartReadinessSeverity.WARNING,
          FermentationStartReadinessCheckCode.PH_HIGH_MICROBIAL_RISK,
          "pH is above the curated cider guidance range; review microbial-risk management before start.",
          value
      );
    }

    return check(
        FermentationStartReadinessSeverity.INFO,
        FermentationStartReadinessCheckCode.PH_ACCEPTABLE,
        "pH is present and within the curated cider fermentation-start guidance range.",
        value
    );
  }

  private static void validateThresholds(
      @NonNull BigDecimal genericYeastToleranceMin,
      @NonNull CiderPhGuidanceRange ciderGuidance,
      @NonNull BigDecimal highMicrobialRisk
  ) {
    if (isGreaterThan(genericYeastToleranceMin, ciderGuidance.minimum())
        || isGreaterThan(ciderGuidance.maximum(), highMicrobialRisk)) {
      throw new DomainRuleViolationException("pH fermentation-start thresholds must be ordered");
    }
  }

  private static @NonNull FermentationStartReadinessCheck check(
      @NonNull FermentationStartReadinessSeverity severity,
      @NonNull FermentationStartReadinessCheckCode code,
      @NonNull String message,
      @Nullable BigDecimal value
  ) {
    return new FermentationStartReadinessCheck(
        FermentationStartReadinessFactor.PH,
        severity,
        code,
        message,
        value,
        PH_UNIT
    );
  }

  private static @NonNull FermentationStartReadinessCheck managedHighPhCheck(
      @NonNull FermentationStartReadinessCheckCode code,
      @NonNull String message,
      @NonNull BigDecimal value,
      @NonNull FermentationStartPreparationActionType action
  ) {
    return new FermentationStartReadinessCheck(
        FermentationStartReadinessFactor.PH,
        FermentationStartReadinessSeverity.WARNING,
        code,
        message,
        value,
        PH_UNIT,
        action
    );
  }

  private static @Nullable FermentationStartPreparationActionType highPhRiskManagementAction(
      @NonNull Set<@NonNull FermentationStartPreparationActionType> actions
  ) {
    requireNonNull(actions, "pH corrective actions cannot be null");
    return actions.stream()
        .filter(PhFermentationStartPolicy::isHighPhRiskManagementAction)
        .min(FermentationStartPreparationActionType::compareTo)
        .orElse(null); // NOSONAR
  }

  private static boolean isHighPhRiskManagementAction(@NonNull FermentationStartPreparationActionType action) {
    return switch (action) {
      case SO2_ADDITION_PLANNED,
          SO2_ADDITION_CONFIRMED,
          ACID_ADJUSTMENT_PLANNED,
          ACID_ADJUSTMENT_CONFIRMED -> true;
      case YEAST_SELECTED,
          PH_REMEASURED,
          YEAST_NUTRIENT_ADDITION_PLANNED,
          YEAST_NUTRIENT_ADDITION_CONFIRMED,
          OXYGENATION_PLANNED,
          OXYGENATION_CONFIRMED,
          YAN_REMEASURED -> false;
    };
  }
}
