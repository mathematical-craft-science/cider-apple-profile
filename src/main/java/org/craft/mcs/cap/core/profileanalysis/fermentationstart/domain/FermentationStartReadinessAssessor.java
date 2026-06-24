package org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredCharacteristic;
import org.craft.mcs.cap.shared.kernel.measurement.RangeMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.StatisticalMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.unit.BeverageUnits;
import org.jmolecules.ddd.annotation.Service;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;


/**
 * Rule-based fermentation-start readiness assessor for feedstock juice/must.
 *
 * <p>This strategy intentionally uses conservative, explainable thresholds instead of a yeast-aware
 * fermentation model. It checks only the current chemical profile dimensions that matter before a
 * cidermaker starts fermentation: sugar availability, pH risk visibility, and yeast-assimilable
 * nitrogen. Future yeast catalog and fermentation-plan work can replace this assessor through the
 * same application boundary with a strain-aware strategy.
 *
 * <p>The YAN thresholds are first-pass cider/wine fermentation heuristics. They must later become
 * configurable by yeast strain, original gravity, nutrient plan, and production method.
 */
@Service
public final class FermentationStartReadinessAssessor {

  public static final @NonNull String METHOD = "RULE_BASED_CIDER_YAN_V1";

  private static final @NonNull BigDecimal LOW_YAN_BLOCKER = new BigDecimal("75");
  private static final @NonNull BigDecimal LOW_YAN_WARNING = new BigDecimal("140");
  private static final @NonNull BigDecimal HIGH_YAN_WARNING = new BigDecimal("350");
  private static final @NonNull BigDecimal LOW_PH_WARNING = new BigDecimal("2.9");
  private static final @NonNull BigDecimal HIGH_PH_WARNING = new BigDecimal("4.0");
  private static final @NonNull String SUGAR_UNIT = BeverageUnits.label(BeverageUnits.SG);
  private static final @NonNull String PH_UNIT = BeverageUnits.label(BeverageUnits.PH);
  private static final @NonNull String YAN_UNIT = BeverageUnits.label(BeverageUnits.YAN);

  public @NonNull FermentationStartReadinessAssessment assess(@NonNull ChemicalProfile profile) {
    requireNonNull(profile, "chemical profile cannot be null");

    final var checks = new ArrayList<FermentationStartReadinessCheck>();
    checks.add(sugarCheck(profile));
    checks.add(phCheck(profile));
    checks.add(yanCheck(profile));

    return new FermentationStartReadinessAssessment(statusOf(checks), METHOD, checks);
  }

  private static @NonNull FermentationStartReadinessCheck sugarCheck(@NonNull ChemicalProfile profile) {
    if (profile.sugar().isEmpty()) {
      return check(
          FermentationStartReadinessFactor.SUGAR,
          FermentationStartReadinessSeverity.BLOCKER,
          "SUGAR_MISSING",
          "Sugar measurement is required before fermentation start assessment.",
          null,
          SUGAR_UNIT
      );
    }

    return check(
        FermentationStartReadinessFactor.SUGAR,
        FermentationStartReadinessSeverity.INFO,
        "SUGAR_PRESENT",
        "Sugar measurement is present and can support fermentation planning.",
        null,
        SUGAR_UNIT
    );
  }

  private static @NonNull FermentationStartReadinessCheck phCheck(@NonNull ChemicalProfile profile) {
    final var value = profile.ph()
        .map(characteristic -> representativeValue(characteristic, PhValue::asBigDecimal))
        .orElse(null);

    if (value == null) {
      return check(
          FermentationStartReadinessFactor.PH,
          FermentationStartReadinessSeverity.WARNING,
          "PH_MISSING",
          "pH is missing; microbial and yeast-stress risks cannot be assessed.",
          null,
          PH_UNIT
      );
    }

    if (value.compareTo(LOW_PH_WARNING) < 0) {
      return check(
          FermentationStartReadinessFactor.PH,
          FermentationStartReadinessSeverity.WARNING,
          "PH_LOW",
          "pH is low and may increase yeast stress during fermentation start.",
          value,
          PH_UNIT
      );
    }

    if (value.compareTo(HIGH_PH_WARNING) > 0) {
      return check(
          FermentationStartReadinessFactor.PH,
          FermentationStartReadinessSeverity.WARNING,
          "PH_HIGH",
          "pH is high; microbial risk should be managed before fermentation start.",
          value,
          PH_UNIT
      );
    }

    return check(
        FermentationStartReadinessFactor.PH,
        FermentationStartReadinessSeverity.INFO,
        "PH_ACCEPTABLE",
        "pH is present and within the initial low-risk heuristic range.",
        value,
        PH_UNIT
    );
  }

  private static @NonNull FermentationStartReadinessCheck yanCheck(@NonNull ChemicalProfile profile) {
    final var value = profile.yeastAssimilableNitrogen()
        .map(characteristic -> representativeValue(characteristic, YeastAssimilableNitrogenValue::asBigDecimal))
        .orElse(null);

    if (value == null) {
      return check(
          FermentationStartReadinessFactor.YEAST_ASSIMILABLE_NITROGEN,
          FermentationStartReadinessSeverity.WARNING,
          "YAN_MISSING",
          "YAN is missing; nutrient deficiency and stuck fermentation risk cannot be assessed.",
          null,
          YAN_UNIT
      );
    }

    if (value.compareTo(LOW_YAN_BLOCKER) < 0) {
      return check(
          FermentationStartReadinessFactor.YEAST_ASSIMILABLE_NITROGEN,
          FermentationStartReadinessSeverity.BLOCKER,
          "YAN_SEVERELY_LOW",
          "YAN is severely low; define a nutrient plan before starting fermentation.",
          value,
          YAN_UNIT
      );
    }

    if (value.compareTo(LOW_YAN_WARNING) < 0) {
      return check(
          FermentationStartReadinessFactor.YEAST_ASSIMILABLE_NITROGEN,
          FermentationStartReadinessSeverity.WARNING,
          "YAN_LOW",
          "YAN is low; monitor fermentation kinetics and consider yeast nutrient planning.",
          value,
          YAN_UNIT
      );
    }

    if (value.compareTo(HIGH_YAN_WARNING) > 0) {
      return check(
          FermentationStartReadinessFactor.YEAST_ASSIMILABLE_NITROGEN,
          FermentationStartReadinessSeverity.WARNING,
          "YAN_HIGH",
          "YAN is high; avoid unnecessary nutrient additions and manage spoilage risk.",
          value,
          YAN_UNIT
      );
    }

    return check(
        FermentationStartReadinessFactor.YEAST_ASSIMILABLE_NITROGEN,
        FermentationStartReadinessSeverity.INFO,
        "YAN_ACCEPTABLE",
        "YAN is within the initial cider fermentation-start heuristic range.",
        value,
        YAN_UNIT
    );
  }

  private static @NonNull FermentationStartReadinessStatus statusOf(
      @NonNull List<@NonNull FermentationStartReadinessCheck> checks
  ) {
    if (checks.stream().anyMatch(check -> check.severity() == FermentationStartReadinessSeverity.BLOCKER)) {
      return FermentationStartReadinessStatus.NOT_READY;
    }
    if (checks.stream().anyMatch(check -> check.severity() == FermentationStartReadinessSeverity.WARNING)) {
      return FermentationStartReadinessStatus.NEEDS_ATTENTION;
    }
    return FermentationStartReadinessStatus.READY;
  }

  private static @NonNull FermentationStartReadinessCheck check(
      @NonNull FermentationStartReadinessFactor factor,
      @NonNull FermentationStartReadinessSeverity severity,
      @NonNull String code,
      @NonNull String message,
      @Nullable BigDecimal value,
      @Nullable String unit
  ) {
    return new FermentationStartReadinessCheck(factor, severity, code, message, value, unit);
  }

  private static <T extends Comparable<T>> @Nullable BigDecimal representativeValue(
      @Nullable MeasuredCharacteristic<T> characteristic,
      @NonNull Function<@NonNull T, @NonNull BigDecimal> extractor
  ) {
    requireNonNull(extractor, "representative value extractor cannot be null");

    return switch (characteristic) {
      case null -> null;
      case ExactMeasurement<T>(T value) -> extractor.apply(value);
      case RangeMeasurement<T>(T min, T max) -> extractor.apply(min)
          .add(extractor.apply(max))
          .divide(BigDecimal.valueOf(2), 6, RoundingMode.HALF_UP);
      case StatisticalMeasurement<T> statistical -> extractor.apply(representativeStatisticalValue(statistical));
    };
  }

  private static <T extends Comparable<T>> @NonNull T representativeStatisticalValue(
      @NonNull StatisticalMeasurement<T> measurement
  ) {
    requireNonNull(measurement, "statistical measurement cannot be null");
    final var median = measurement.median();
    return requireNonNullElse(median, measurement.mean()); // NOSONAR
  }
}
