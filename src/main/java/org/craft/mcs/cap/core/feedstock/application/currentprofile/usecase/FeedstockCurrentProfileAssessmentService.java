package org.craft.mcs.cap.core.feedstock.application.currentprofile.usecase;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.profileanalysis.ph.domain.PhPlanningGuidance;
import org.craft.mcs.cap.core.profileanalysis.ph.domain.PhPlanningGuidanceCode;
import org.craft.mcs.cap.core.profileanalysis.ph.domain.PhPlanningGuidancePolicy;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileEvaluation;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.port.in.FeedstockCurrentProfileDataQualityIndicator;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.port.in.FeedstockCurrentProfileDataQualityIndicatorCode;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.port.in.FeedstockCurrentProfileSignalSeverity;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.port.in.FeedstockCurrentProfileDataQualityStatus;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.port.in.FeedstockCurrentProfileDataQualityView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.port.in.FeedstockCurrentMeasurementFreshnessStatus;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.port.in.FeedstockCurrentMeasurementFreshnessView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.port.in.FeedstockPlanningReadinessCheck;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.port.in.FeedstockPlanningReadinessFactor;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.port.in.FeedstockPlanningReadinessStatus;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.port.in.FeedstockPlanningReadinessView;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredCharacteristic;
import org.craft.mcs.cap.shared.kernel.measurement.RangeMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.StatisticalMeasurement;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * Builds current-profile diagnostics from the latest feedstock batch evidence.
 *
 * <p>The service belongs to application read-side logic: it does not change aggregates and does
 * not decide for the technologist. It only summarizes freshness, planning readiness and data
 * quality in a compact form that application read models can reuse consistently.
 */
@Component
@RequiredArgsConstructor
public final class FeedstockCurrentProfileAssessmentService {

  private static final @NonNull String STALE_MEASUREMENT_REASON =
      "Current measurement is older than configured freshness threshold";

  private final @NonNull Clock clock;
  private final @NonNull FeedstockCurrentMeasurementFreshnessPolicy measurementFreshnessPolicy;
  private final @NonNull PhPlanningGuidancePolicy phPlanningGuidancePolicy;

  public @NonNull FeedstockCurrentMeasurementFreshnessView freshness(
      @NonNull FeedstockMeasurement measurement
  ) {
    requireNonNull(measurement, "feedstock measurement cannot be null");
    final var stale = measurementFreshnessPolicy.isStale(measurement.measuredAt(), clock.instant());
    if (!stale) {
      return new FeedstockCurrentMeasurementFreshnessView(
          FeedstockCurrentMeasurementFreshnessStatus.CURRENT,
          false,
          null
      );
    }

    return new FeedstockCurrentMeasurementFreshnessView(
        FeedstockCurrentMeasurementFreshnessStatus.STALE,
        true,
        STALE_MEASUREMENT_REASON
    );
  }

  public @NonNull FeedstockPlanningReadinessView planningReadinessWithoutMeasurement(
      @NonNull FeedstockBatchSummary batch
  ) {
    final var checks = new ArrayList<FeedstockPlanningReadinessCheck>();
    checks.add(planningCheck(
        FeedstockPlanningReadinessFactor.CURRENT_MEASUREMENT,
        FeedstockCurrentProfileSignalSeverity.BLOCKING,
        "CURRENT_MEASUREMENT_MISSING",
        "Batch has no current measurement"
    ));
    checks.add(volumeCheck(batch));

    return new FeedstockPlanningReadinessView(planningStatus(checks), checks);
  }

  public @NonNull FeedstockPlanningReadinessView planningReadiness(
      @NonNull FeedstockBatchSummary batch,
      @NonNull ChemicalProfile profile
  ) {
    requireNonNull(batch, "feedstock batch summary cannot be null");
    requireNonNull(profile, "chemical profile cannot be null");

    final var checks = new ArrayList<FeedstockPlanningReadinessCheck>();
    checks.add(planningCheck(
        FeedstockPlanningReadinessFactor.CURRENT_MEASUREMENT,
        FeedstockCurrentProfileSignalSeverity.OK,
        "CURRENT_MEASUREMENT_PRESENT",
        "Current measurement is available"
    ));
    checks.add(volumeCheck(batch));
    checks.add(requiredProfileCheck(
        profile.sugar().isPresent(),
        FeedstockPlanningReadinessFactor.SUGAR,
        "SUGAR_PRESENT",
        "SUGAR_MISSING",
        "Sugar is available",
        "Sugar is required for composition planning"
    ));
    checks.add(requiredProfileCheck(
        profile.titratableAcidity().isPresent(),
        FeedstockPlanningReadinessFactor.TITRATABLE_ACIDITY,
        "TITRATABLE_ACIDITY_PRESENT",
        "TITRATABLE_ACIDITY_MISSING",
        "Titratable acidity is available",
        "Titratable acidity is required for composition planning"
    ));
    checks.add(planningCheck(
        FeedstockPlanningReadinessFactor.TANNIN,
        profile.tanninity().isPresent()
            ? FeedstockCurrentProfileSignalSeverity.OK
            : FeedstockCurrentProfileSignalSeverity.WARNING,
        profile.tanninity().isPresent() ? "TANNIN_PRESENT" : "TANNIN_MISSING",
        profile.tanninity().isPresent()
            ? "Tannin is available"
            : "Tannin is missing; target-aware structure assessment will be incomplete"
    ));
    checks.add(phPlanningGuidanceCheck(phPlanningGuidancePolicy.assess(profile)));
    return new FeedstockPlanningReadinessView(planningStatus(checks), checks);
  }

  public @NonNull FeedstockCurrentProfileDataQualityView dataQualityWithoutMeasurement() {
    final var indicators = List.of(dataQualityIndicator(
        FeedstockCurrentProfileDataQualityIndicatorCode.CURRENT_MEASUREMENT_MISSING,
        FeedstockCurrentProfileSignalSeverity.BLOCKING,
        "Batch has no current measurement"
    ));
    return new FeedstockCurrentProfileDataQualityView(FeedstockCurrentProfileDataQualityStatus.BLOCKED, indicators);
  }

  public @NonNull FeedstockCurrentProfileDataQualityView dataQuality(
      @NonNull ChemicalProfile profile,
      @NonNull SugarAcidityProfileEvaluation evaluation,
      @NonNull FeedstockCurrentMeasurementFreshnessView freshness
  ) {
    requireNonNull(profile, "chemical profile cannot be null");
    requireNonNull(evaluation, "sugar-acidity profile evaluation cannot be null");
    requireNonNull(freshness, "feedstock measurement freshness cannot be null");

    final var indicators = new ArrayList<FeedstockCurrentProfileDataQualityIndicator>();
    addSugarAcidityQuality(indicators, evaluation);
    addOptionalAxisQuality(indicators, profile);
    addSourceQuality(indicators, profile);
    addConfidenceQuality(indicators, profile);
    addNonPointValueQuality(indicators, profile);
    if (freshness.stale()) {
      indicators.add(dataQualityIndicator(
          FeedstockCurrentProfileDataQualityIndicatorCode.MEASUREMENT_STALE,
          FeedstockCurrentProfileSignalSeverity.WARNING,
          "Current measurement is stale"
      ));
    }

    return new FeedstockCurrentProfileDataQualityView(dataQualityStatus(indicators), indicators);
  }

  private static void addSugarAcidityQuality(
      @NonNull List<FeedstockCurrentProfileDataQualityIndicator> indicators,
      @NonNull SugarAcidityProfileEvaluation evaluation
  ) {
    if (evaluation.plottable()) {
      indicators.add(dataQualityIndicator(
          FeedstockCurrentProfileDataQualityIndicatorCode.SUGAR_ACIDITY_PLOTTABLE,
          FeedstockCurrentProfileSignalSeverity.INFO,
          "Sugar-acidity point is plottable"
      ));
      return;
    }

    if (evaluation.capabilityAssessment().missingAxes().contains(ChemicalAxis.SUGAR)) {
      indicators.add(dataQualityIndicator(
          FeedstockCurrentProfileDataQualityIndicatorCode.SUGAR_MISSING,
          FeedstockCurrentProfileSignalSeverity.BLOCKING,
          "Sugar is missing"
      ));
    }
    if (evaluation.capabilityAssessment().missingAxes().contains(ChemicalAxis.ACIDITY)) {
      indicators.add(dataQualityIndicator(
          FeedstockCurrentProfileDataQualityIndicatorCode.TITRATABLE_ACIDITY_MISSING,
          FeedstockCurrentProfileSignalSeverity.BLOCKING,
          "Titratable acidity is missing"
      ));
    }
  }

  private static void addOptionalAxisQuality(
      @NonNull List<FeedstockCurrentProfileDataQualityIndicator> indicators,
      @NonNull ChemicalProfile profile
  ) {
    if (profile.tanninity().isEmpty()) {
      indicators.add(dataQualityIndicator(
          FeedstockCurrentProfileDataQualityIndicatorCode.TANNIN_MISSING,
          FeedstockCurrentProfileSignalSeverity.WARNING,
          "Tannin is missing"
      ));
    }
    if (profile.ph().isEmpty()) {
      indicators.add(dataQualityIndicator(
          FeedstockCurrentProfileDataQualityIndicatorCode.PH_MISSING,
          FeedstockCurrentProfileSignalSeverity.WARNING,
          "pH is missing"
      ));
    }
    if (profile.yeastAssimilableNitrogen().isEmpty()) {
      indicators.add(dataQualityIndicator(
          FeedstockCurrentProfileDataQualityIndicatorCode.YAN_MISSING,
          FeedstockCurrentProfileSignalSeverity.WARNING,
          "YAN is missing"
      ));
    }
  }

  private static void addSourceQuality(
      @NonNull List<FeedstockCurrentProfileDataQualityIndicator> indicators,
      @NonNull ChemicalProfile profile
  ) {
    if (profile.source() == null) {
      indicators.add(dataQualityIndicator(
          FeedstockCurrentProfileDataQualityIndicatorCode.SOURCE_MISSING,
          FeedstockCurrentProfileSignalSeverity.WARNING,
          "Measurement source is missing"
      ));
    }
  }

  private static void addConfidenceQuality(
      @NonNull List<FeedstockCurrentProfileDataQualityIndicator> indicators,
      @NonNull ChemicalProfile profile
  ) {
    final var confidence = profile.confidence();
    if (confidence == null) {
      indicators.add(dataQualityIndicator(
          FeedstockCurrentProfileDataQualityIndicatorCode.CONFIDENCE_MISSING,
          FeedstockCurrentProfileSignalSeverity.WARNING,
          "Measurement confidence is missing"
      ));
      return;
    }
    if (confidence == ConfidenceLevel.LOW) {
      indicators.add(dataQualityIndicator(
          FeedstockCurrentProfileDataQualityIndicatorCode.LOW_CONFIDENCE,
          FeedstockCurrentProfileSignalSeverity.WARNING,
          "Measurement confidence is low"
      ));
    }
  }

  private static void addNonPointValueQuality(
      @NonNull List<FeedstockCurrentProfileDataQualityIndicator> indicators,
      @NonNull ChemicalProfile profile
  ) {
    if (hasNonPointValues(profile)) {
      indicators.add(dataQualityIndicator(
          FeedstockCurrentProfileDataQualityIndicatorCode.NON_POINT_VALUES,
          FeedstockCurrentProfileSignalSeverity.INFO,
          "Current profile contains range or statistical values"
      ));
    }
  }

  private static boolean hasNonPointValues(@NonNull ChemicalProfile profile) {
    return profile.sugar().map(FeedstockCurrentProfileAssessmentService::isNonPoint).orElse(false)
        || profile.ph().map(FeedstockCurrentProfileAssessmentService::isNonPoint).orElse(false)
        || profile.titratableAcidity().map(FeedstockCurrentProfileAssessmentService::isNonPoint).orElse(false)
        || profile.tanninity().map(FeedstockCurrentProfileAssessmentService::isNonPoint).orElse(false)
        || profile.yeastAssimilableNitrogen()
            .map(FeedstockCurrentProfileAssessmentService::isNonPoint)
            .orElse(false);
  }

  private static <T extends Comparable<T>> boolean isNonPoint(
      @NonNull MeasuredCharacteristic<T> measurement
  ) {
    return switch (measurement) {
      case ExactMeasurement<T> _ -> false;
      case RangeMeasurement<T> _ -> true;
      case StatisticalMeasurement<T> _ -> true;
    };
  }

  private static @NonNull FeedstockPlanningReadinessCheck volumeCheck(
      @NonNull FeedstockBatchSummary batch
  ) {
    requireNonNull(batch, "feedstock batch summary cannot be null");
    return planningCheck(
        FeedstockPlanningReadinessFactor.VOLUME,
        batch.availableVolume().isZero()
            ? FeedstockCurrentProfileSignalSeverity.BLOCKING
            : FeedstockCurrentProfileSignalSeverity.OK,
        batch.availableVolume().isZero() ? "AVAILABLE_VOLUME_EMPTY" : "AVAILABLE_VOLUME_PRESENT",
        batch.availableVolume().isZero()
            ? "Available volume is zero"
            : "Available volume is present"
    );
  }

  private static @NonNull FeedstockPlanningReadinessCheck requiredProfileCheck(
      boolean present,
      @NonNull FeedstockPlanningReadinessFactor factor,
      @NonNull String presentCode,
      @NonNull String missingCode,
      @NonNull String presentMessage,
      @NonNull String missingMessage
  ) {
    return planningCheck(
        factor,
        present ? FeedstockCurrentProfileSignalSeverity.OK : FeedstockCurrentProfileSignalSeverity.BLOCKING,
        present ? presentCode : missingCode,
        present ? presentMessage : missingMessage
    );
  }

  private static @NonNull FeedstockPlanningReadinessCheck phPlanningGuidanceCheck(
      @NonNull PhPlanningGuidance guidance
  ) {
    requireNonNull(guidance, "pH planning guidance cannot be null");
    return planningCheck(
        FeedstockPlanningReadinessFactor.PH,
        phPlanningSeverity(guidance.code()),
        guidance.code().name(),
        guidance.message()
    );
  }

  private static @NonNull FeedstockCurrentProfileSignalSeverity phPlanningSeverity(
      @NonNull PhPlanningGuidanceCode code
  ) {
    requireNonNull(code, "pH planning guidance code cannot be null");
    return switch (code) {
      case PH_PLANNING_IDEAL -> FeedstockCurrentProfileSignalSeverity.OK;
      case PH_PLANNING_LOW, PH_PLANNING_HIGH, PH_MISSING -> FeedstockCurrentProfileSignalSeverity.WARNING;
    };
  }

  private static @NonNull FeedstockPlanningReadinessCheck planningCheck(
      @NonNull FeedstockPlanningReadinessFactor factor,
      @NonNull FeedstockCurrentProfileSignalSeverity severity,
      @NonNull String code,
      @NonNull String message
  ) {
    return new FeedstockPlanningReadinessCheck(factor, severity, code, message);
  }

  private static @NonNull FeedstockPlanningReadinessStatus planningStatus(
      @NonNull List<FeedstockPlanningReadinessCheck> checks
  ) {
    if (checks.stream().anyMatch(check -> check.severity() == FeedstockCurrentProfileSignalSeverity.BLOCKING)) {
      return FeedstockPlanningReadinessStatus.BLOCKED;
    }
    if (checks.stream().anyMatch(check -> check.severity() == FeedstockCurrentProfileSignalSeverity.WARNING)) {
      return FeedstockPlanningReadinessStatus.NEEDS_ATTENTION;
    }
    return FeedstockPlanningReadinessStatus.READY;
  }

  private static @NonNull FeedstockCurrentProfileDataQualityIndicator dataQualityIndicator(
      @NonNull FeedstockCurrentProfileDataQualityIndicatorCode code,
      @NonNull FeedstockCurrentProfileSignalSeverity severity,
      @NonNull String message
  ) {
    return new FeedstockCurrentProfileDataQualityIndicator(code, severity, message);
  }

  private static @NonNull FeedstockCurrentProfileDataQualityStatus dataQualityStatus(
      @NonNull List<FeedstockCurrentProfileDataQualityIndicator> indicators
  ) {
    if (indicators.stream()
        .anyMatch(indicator -> indicator.severity() == FeedstockCurrentProfileSignalSeverity.BLOCKING)) {
      return FeedstockCurrentProfileDataQualityStatus.BLOCKED;
    }
    if (indicators.stream()
        .anyMatch(indicator -> indicator.severity() == FeedstockCurrentProfileSignalSeverity.WARNING)) {
      return FeedstockCurrentProfileDataQualityStatus.NEEDS_ATTENTION;
    }
    return FeedstockCurrentProfileDataQualityStatus.READY;
  }
}
