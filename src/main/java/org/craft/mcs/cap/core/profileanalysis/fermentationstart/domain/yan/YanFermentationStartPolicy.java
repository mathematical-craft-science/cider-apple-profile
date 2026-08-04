package org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.yan;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Set;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartPreparationActionType;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessCheck;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessCheckCode;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessFactor;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessSeverity;
import org.craft.mcs.cap.core.profileanalysis.yan.domain.YanRuleSource;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.unit.BeverageUnits;
import org.jmolecules.ddd.annotation.Service;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasurementValueMath.midpoint;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isGreaterThan;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isLessThan;


/**
 * YAN readiness policy for the moment immediately before fermentation start.
 *
 * <p>The default v1 thresholds are curated cider/enology business rules. YAN need depends on
 * original gravity, yeast strain, nutrient strategy, temperature and production method, so this
 * policy must be treated as guidance, not as a universal fermentation law.
 */
@Service
public record YanFermentationStartPolicy(
    @NonNull BigDecimal severeLow,
    @NonNull BigDecimal low,
    @NonNull BigDecimal high,
    @NonNull YanRuleSource source
) {

  private static final @NonNull BigDecimal DEFAULT_SEVERE_LOW = new BigDecimal("75");
  private static final @NonNull BigDecimal DEFAULT_LOW = new BigDecimal("150");
  private static final @NonNull BigDecimal DEFAULT_HIGH = new BigDecimal("400");
  private static final @NonNull String YAN_UNIT = BeverageUnits.label(BeverageUnits.YAN);

  public YanFermentationStartPolicy {
    requireNonNull(severeLow, "severe low YAN threshold cannot be null");
    requireNonNull(low, "low YAN threshold cannot be null");
    requireNonNull(high, "high YAN threshold cannot be null");
    requireNonNull(source, "YAN fermentation-start policy source cannot be null");
    validateThresholds(severeLow, low, high);
  }

  public static @NonNull YanFermentationStartPolicy curatedCiderV1() {
    return new YanFermentationStartPolicy(
        DEFAULT_SEVERE_LOW,
        DEFAULT_LOW,
        DEFAULT_HIGH,
        YanRuleSource.curatedCiderV1()
    );
  }

  public @NonNull FermentationStartReadinessCheck check(
      @Nullable CharacteristicValue<YeastAssimilableNitrogenValue> yeastAssimilableNitrogen
  ) {
    return check(YanFermentationStartContext.withoutPreparationActions(yeastAssimilableNitrogen));
  }

  public @NonNull FermentationStartReadinessCheck check(@NonNull YanFermentationStartContext context) {
    requireNonNull(context, "YAN fermentation-start context cannot be null");
    final var yan = context.yeastAssimilableNitrogen();
    final var value = yan == null ? null : midpoint(yan);
    if (value == null) {
      return check(
          FermentationStartReadinessSeverity.WARNING,
          FermentationStartReadinessCheckCode.YAN_MISSING,
          "YAN is missing; nutrient deficiency and stuck fermentation risk cannot be assessed.",
          null
      );
    }

    if (isLessThan(value, severeLow)) {
      final var action = nutrientManagementAction(context.preparationActions());
      if (action != null) {
        return managedYanCheck(
            FermentationStartReadinessCheckCode.YAN_SEVERELY_LOW_MANAGED,
            "YAN is severely low; nutrient risk is marked as managed by a preparation action.",
            value,
            action
        );
      }
      return check(
          FermentationStartReadinessSeverity.BLOCKER,
          FermentationStartReadinessCheckCode.YAN_SEVERELY_LOW,
          "YAN is severely low; define a nutrient plan before starting fermentation.",
          value
      );
    }

    if (isLessThan(value, low)) {
      final var action = nutrientManagementAction(context.preparationActions());
      if (action != null) {
        return managedYanCheck(
            FermentationStartReadinessCheckCode.YAN_LOW_MANAGED,
            "YAN is low; nutrient risk is marked as managed by a preparation action.",
            value,
            action
        );
      }
      return check(
          FermentationStartReadinessSeverity.WARNING,
          FermentationStartReadinessCheckCode.YAN_LOW,
          "YAN is low; monitor fermentation kinetics and consider yeast nutrient planning.",
          value
      );
    }

    if (isGreaterThan(value, high)) {
      return check(
          FermentationStartReadinessSeverity.WARNING,
          FermentationStartReadinessCheckCode.YAN_HIGH,
          "YAN is high; avoid unnecessary nutrient additions and manage microbial stability risk.",
          value
      );
    }

    return check(
        FermentationStartReadinessSeverity.INFO,
        FermentationStartReadinessCheckCode.YAN_ACCEPTABLE,
        "YAN is within the curated cider fermentation-start guidance range.",
        value
    );
  }

  private static void validateThresholds(
      @NonNull BigDecimal severeLow,
      @NonNull BigDecimal low,
      @NonNull BigDecimal high
  ) {
    if (isGreaterThan(severeLow, low) || isGreaterThan(low, high)) {
      throw new DomainRuleViolationException("YAN fermentation-start thresholds must be ordered");
    }
  }

  private static @NonNull FermentationStartReadinessCheck check(
      @NonNull FermentationStartReadinessSeverity severity,
      @NonNull FermentationStartReadinessCheckCode code,
      @NonNull String message,
      @Nullable BigDecimal value
  ) {
    return new FermentationStartReadinessCheck(
        FermentationStartReadinessFactor.YEAST_ASSIMILABLE_NITROGEN,
        severity,
        code,
        message,
        value,
        YAN_UNIT
    );
  }

  private static @NonNull FermentationStartReadinessCheck managedYanCheck(
      @NonNull FermentationStartReadinessCheckCode code,
      @NonNull String message,
      @NonNull BigDecimal value,
      @NonNull FermentationStartPreparationActionType action
  ) {
    return new FermentationStartReadinessCheck(
        FermentationStartReadinessFactor.YEAST_ASSIMILABLE_NITROGEN,
        FermentationStartReadinessSeverity.WARNING,
        code,
        message,
        value,
        YAN_UNIT,
        action
    );
  }

  private static @Nullable FermentationStartPreparationActionType nutrientManagementAction(
      @NonNull Set<@NonNull FermentationStartPreparationActionType> actions
  ) {
    requireNonNull(actions, "YAN preparation actions cannot be null");
    return actions.stream()
        .filter(YanFermentationStartPolicy::isNutrientManagementAction)
        .min(Comparator.comparing(FermentationStartPreparationActionType::ordinal))
        .orElse(null); // NOSONAR
  }

  private static boolean isNutrientManagementAction(@NonNull FermentationStartPreparationActionType action) {
    return switch (action) {
      case YEAST_NUTRIENT_ADDITION_PLANNED,
          YEAST_NUTRIENT_ADDITION_CONFIRMED -> true;
      case YEAST_SELECTED,
          SO2_ADDITION_PLANNED,
          SO2_ADDITION_CONFIRMED,
          ACID_ADJUSTMENT_PLANNED,
          ACID_ADJUSTMENT_CONFIRMED,
          PH_REMEASURED,
          OXYGENATION_PLANNED,
          OXYGENATION_CONFIRMED,
          YAN_REMEASURED -> false;
    };
  }
}
