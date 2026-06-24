package org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.usecase;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileEvaluation;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningDataQualityIndicator;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningDataQualityIndicatorCode;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningSignalSeverity;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningDataQualityStatus;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningDataQualityView;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningMeasurementFreshnessStatus;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningMeasurementFreshnessView;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningReadinessCheck;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningReadinessFactor;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningReadinessStatus;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningReadinessView;
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
 * Builds feedstock planning diagnostics from current batch evidence.
 *
 * <p>The service belongs to application read-side logic: it does not change aggregates and does
 * not decide for the technologist. It only summarizes freshness, planning readiness and data
 * quality in a compact form that API clients can render consistently.
 */
@Component
@RequiredArgsConstructor
public final class FeedstockPlanningBatchAssessmentService {

  private static final @NonNull String STALE_MEASUREMENT_REASON =
      "Current measurement is older than configured freshness threshold";

  private final @NonNull Clock clock;
  private final @NonNull FeedstockPlanningMeasurementFreshnessPolicy measurementFreshnessPolicy;

  public @NonNull FeedstockPlanningMeasurementFreshnessView freshness(
      @NonNull FeedstockMeasurement measurement
  ) {
    requireNonNull(measurement, "feedstock measurement cannot be null");
    final var stale = measurementFreshnessPolicy.isStale(measurement.measuredAt(), clock.instant());
    if (!stale) {
      return new FeedstockPlanningMeasurementFreshnessView(
          FeedstockPlanningMeasurementFreshnessStatus.CURRENT,
          false,
          null
      );
    }

    return new FeedstockPlanningMeasurementFreshnessView(
        FeedstockPlanningMeasurementFreshnessStatus.STALE,
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
        FeedstockPlanningSignalSeverity.BLOCKING,
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
        FeedstockPlanningSignalSeverity.OK,
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
            ? FeedstockPlanningSignalSeverity.OK
            : FeedstockPlanningSignalSeverity.WARNING,
        profile.tanninity().isPresent() ? "TANNIN_PRESENT" : "TANNIN_MISSING",
        profile.tanninity().isPresent()
            ? "Tannin is available"
            : "Tannin is missing; target-aware structure assessment will be incomplete"
    ));
    return new FeedstockPlanningReadinessView(planningStatus(checks), checks);
  }

  public @NonNull FeedstockPlanningDataQualityView dataQualityWithoutMeasurement() {
    final var indicators = List.of(dataQualityIndicator(
        FeedstockPlanningDataQualityIndicatorCode.CURRENT_MEASUREMENT_MISSING,
        FeedstockPlanningSignalSeverity.BLOCKING,
        "Batch has no current measurement"
    ));
    return new FeedstockPlanningDataQualityView(FeedstockPlanningDataQualityStatus.BLOCKED, indicators);
  }

  public @NonNull FeedstockPlanningDataQualityView dataQuality(
      @NonNull ChemicalProfile profile,
      @NonNull SugarAcidityProfileEvaluation evaluation,
      @NonNull FeedstockPlanningMeasurementFreshnessView freshness
  ) {
    requireNonNull(profile, "chemical profile cannot be null");
    requireNonNull(evaluation, "sugar-acidity profile evaluation cannot be null");
    requireNonNull(freshness, "feedstock measurement freshness cannot be null");

    final var indicators = new ArrayList<FeedstockPlanningDataQualityIndicator>();
    addSugarAcidityQuality(indicators, evaluation);
    addOptionalAxisQuality(indicators, profile);
    addSourceQuality(indicators, profile);
    addConfidenceQuality(indicators, profile);
    addNonPointValueQuality(indicators, profile);
    if (freshness.stale()) {
      indicators.add(dataQualityIndicator(
          FeedstockPlanningDataQualityIndicatorCode.MEASUREMENT_STALE,
          FeedstockPlanningSignalSeverity.WARNING,
          "Current measurement is stale"
      ));
    }

    return new FeedstockPlanningDataQualityView(dataQualityStatus(indicators), indicators);
  }

  private static void addSugarAcidityQuality(
      @NonNull List<FeedstockPlanningDataQualityIndicator> indicators,
      @NonNull SugarAcidityProfileEvaluation evaluation
  ) {
    if (evaluation.plottable()) {
      indicators.add(dataQualityIndicator(
          FeedstockPlanningDataQualityIndicatorCode.SUGAR_ACIDITY_PLOTTABLE,
          FeedstockPlanningSignalSeverity.INFO,
          "Sugar-acidity point is plottable"
      ));
      return;
    }

    if (evaluation.capabilityAssessment().missingAxes().contains(ChemicalAxis.SUGAR)) {
      indicators.add(dataQualityIndicator(
          FeedstockPlanningDataQualityIndicatorCode.SUGAR_MISSING,
          FeedstockPlanningSignalSeverity.BLOCKING,
          "Sugar is missing"
      ));
    }
    if (evaluation.capabilityAssessment().missingAxes().contains(ChemicalAxis.ACIDITY)) {
      indicators.add(dataQualityIndicator(
          FeedstockPlanningDataQualityIndicatorCode.TITRATABLE_ACIDITY_MISSING,
          FeedstockPlanningSignalSeverity.BLOCKING,
          "Titratable acidity is missing"
      ));
    }
  }

  private static void addOptionalAxisQuality(
      @NonNull List<FeedstockPlanningDataQualityIndicator> indicators,
      @NonNull ChemicalProfile profile
  ) {
    if (profile.tanninity().isEmpty()) {
      indicators.add(dataQualityIndicator(
          FeedstockPlanningDataQualityIndicatorCode.TANNIN_MISSING,
          FeedstockPlanningSignalSeverity.WARNING,
          "Tannin is missing"
      ));
    }
    if (profile.ph().isEmpty()) {
      indicators.add(dataQualityIndicator(
          FeedstockPlanningDataQualityIndicatorCode.PH_MISSING,
          FeedstockPlanningSignalSeverity.WARNING,
          "pH is missing"
      ));
    }
    if (profile.yeastAssimilableNitrogen().isEmpty()) {
      indicators.add(dataQualityIndicator(
          FeedstockPlanningDataQualityIndicatorCode.YAN_MISSING,
          FeedstockPlanningSignalSeverity.WARNING,
          "YAN is missing"
      ));
    }
  }

  private static void addSourceQuality(
      @NonNull List<FeedstockPlanningDataQualityIndicator> indicators,
      @NonNull ChemicalProfile profile
  ) {
    if (profile.source() == null) {
      indicators.add(dataQualityIndicator(
          FeedstockPlanningDataQualityIndicatorCode.SOURCE_MISSING,
          FeedstockPlanningSignalSeverity.WARNING,
          "Measurement source is missing"
      ));
    }
  }

  private static void addConfidenceQuality(
      @NonNull List<FeedstockPlanningDataQualityIndicator> indicators,
      @NonNull ChemicalProfile profile
  ) {
    final var confidence = profile.confidence();
    if (confidence == null) {
      indicators.add(dataQualityIndicator(
          FeedstockPlanningDataQualityIndicatorCode.CONFIDENCE_MISSING,
          FeedstockPlanningSignalSeverity.WARNING,
          "Measurement confidence is missing"
      ));
      return;
    }
    if (confidence == ConfidenceLevel.LOW) {
      indicators.add(dataQualityIndicator(
          FeedstockPlanningDataQualityIndicatorCode.LOW_CONFIDENCE,
          FeedstockPlanningSignalSeverity.WARNING,
          "Measurement confidence is low"
      ));
    }
  }

  private static void addNonPointValueQuality(
      @NonNull List<FeedstockPlanningDataQualityIndicator> indicators,
      @NonNull ChemicalProfile profile
  ) {
    if (hasNonPointValues(profile)) {
      indicators.add(dataQualityIndicator(
          FeedstockPlanningDataQualityIndicatorCode.NON_POINT_VALUES,
          FeedstockPlanningSignalSeverity.INFO,
          "Current profile contains range or statistical values"
      ));
    }
  }

  private static boolean hasNonPointValues(@NonNull ChemicalProfile profile) {
    return profile.sugar().map(FeedstockPlanningBatchAssessmentService::isNonPoint).orElse(false)
        || profile.ph().map(FeedstockPlanningBatchAssessmentService::isNonPoint).orElse(false)
        || profile.titratableAcidity().map(FeedstockPlanningBatchAssessmentService::isNonPoint).orElse(false)
        || profile.tanninity().map(FeedstockPlanningBatchAssessmentService::isNonPoint).orElse(false)
        || profile.yeastAssimilableNitrogen()
            .map(FeedstockPlanningBatchAssessmentService::isNonPoint)
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
            ? FeedstockPlanningSignalSeverity.BLOCKING
            : FeedstockPlanningSignalSeverity.OK,
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
        present ? FeedstockPlanningSignalSeverity.OK : FeedstockPlanningSignalSeverity.BLOCKING,
        present ? presentCode : missingCode,
        present ? presentMessage : missingMessage
    );
  }

  private static @NonNull FeedstockPlanningReadinessCheck planningCheck(
      @NonNull FeedstockPlanningReadinessFactor factor,
      @NonNull FeedstockPlanningSignalSeverity severity,
      @NonNull String code,
      @NonNull String message
  ) {
    return new FeedstockPlanningReadinessCheck(factor, severity, code, message);
  }

  private static @NonNull FeedstockPlanningReadinessStatus planningStatus(
      @NonNull List<FeedstockPlanningReadinessCheck> checks
  ) {
    if (checks.stream().anyMatch(check -> check.severity() == FeedstockPlanningSignalSeverity.BLOCKING)) {
      return FeedstockPlanningReadinessStatus.BLOCKED;
    }
    if (checks.stream().anyMatch(check -> check.severity() == FeedstockPlanningSignalSeverity.WARNING)) {
      return FeedstockPlanningReadinessStatus.NEEDS_ATTENTION;
    }
    return FeedstockPlanningReadinessStatus.READY;
  }

  private static @NonNull FeedstockPlanningDataQualityIndicator dataQualityIndicator(
      @NonNull FeedstockPlanningDataQualityIndicatorCode code,
      @NonNull FeedstockPlanningSignalSeverity severity,
      @NonNull String message
  ) {
    return new FeedstockPlanningDataQualityIndicator(code, severity, message);
  }

  private static @NonNull FeedstockPlanningDataQualityStatus dataQualityStatus(
      @NonNull List<FeedstockPlanningDataQualityIndicator> indicators
  ) {
    if (indicators.stream()
        .anyMatch(indicator -> indicator.severity() == FeedstockPlanningSignalSeverity.BLOCKING)) {
      return FeedstockPlanningDataQualityStatus.BLOCKED;
    }
    if (indicators.stream()
        .anyMatch(indicator -> indicator.severity() == FeedstockPlanningSignalSeverity.WARNING)) {
      return FeedstockPlanningDataQualityStatus.NEEDS_ATTENTION;
    }
    return FeedstockPlanningDataQualityStatus.READY;
  }
}
