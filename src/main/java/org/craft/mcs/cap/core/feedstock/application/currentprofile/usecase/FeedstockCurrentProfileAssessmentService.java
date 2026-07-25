package org.craft.mcs.cap.core.feedstock.application.currentprofile.usecase;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileSettings;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.*;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementView;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockProfileAxisEvidence;
import org.craft.mcs.cap.core.profileanalysis.ph.domain.PhPlanningGuidance;
import org.craft.mcs.cap.core.profileanalysis.ph.domain.PhPlanningGuidanceCode;
import org.craft.mcs.cap.core.profileanalysis.ph.domain.PhPlanningGuidancePolicy;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileEvaluation;
import org.craft.mcs.cap.shared.application.measurement.MeasurementFreshnessStatus;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * Builds current-profile diagnostics from state-aware Feedstock evidence.
 *
 * <p>The service belongs to application read-side logic: it does not change aggregates and does
 * not decide for the technologist. It only summarizes freshness, planning readiness and data
 * quality in a compact form that application read models can reuse consistently.
 */
@Component
@RequiredArgsConstructor
public final class FeedstockCurrentProfileAssessmentService {

  private static final @NonNull String STALE_MEASUREMENT_REASON =
      "Current profile contains planning evidence older than the configured freshness threshold";

  private final @NonNull Clock clock;
  private final @NonNull PhPlanningGuidancePolicy phPlanningGuidancePolicy;

  public @NonNull FeedstockCurrentMeasurementFreshnessView freshness(
      @NonNull FeedstockMeasurementView measurement,
      @NonNull FeedstockCurrentProfileSettings settings
  ) {
    requireNonNull(measurement, "feedstock measurement cannot be null");
    requireNonNull(settings, "feedstock current profile settings cannot be null");
    final var measurementFreshnessPolicy = new FeedstockCurrentMeasurementFreshnessPolicy(
        settings.measurementStaleAfter()
    );
    final var oldestEvidenceAt = measurement.axisEvidence().stream()
        .filter(FeedstockCurrentProfileAssessmentService::isPlanningFreshnessEvidence)
        .map(FeedstockProfileAxisEvidence::measuredAt)
        .min(Instant::compareTo)
        .orElse(measurement.measuredAt());
    final var stale = measurementFreshnessPolicy.isStale(oldestEvidenceAt, clock.instant());
    if (!stale) {
      return new FeedstockCurrentMeasurementFreshnessView(
          MeasurementFreshnessStatus.CURRENT,
          false,
          null
      );
    }

    return new FeedstockCurrentMeasurementFreshnessView(
        MeasurementFreshnessStatus.STALE,
        true,
        STALE_MEASUREMENT_REASON
    );
  }

  public @NonNull List<@NonNull FeedstockCurrentProfileEvidenceView> evidence(
      @NonNull FeedstockMeasurementView measurement,
      @NonNull FeedstockCurrentProfileSettings settings
  ) {
    requireNonNull(measurement, "feedstock measurement cannot be null");
    requireNonNull(settings, "feedstock current profile settings cannot be null");
    final var freshnessPolicy = new FeedstockCurrentMeasurementFreshnessPolicy(
        settings.measurementStaleAfter()
    );
    final var assessedAt = clock.instant();
    return measurement.axisEvidence().stream()
        .map(axis -> {
          final var stale = freshnessPolicy.isStale(axis.measuredAt(), assessedAt);
          return new FeedstockCurrentProfileEvidenceView(
              axis.characteristic(),
              axis.origin(),
              axis.materialStateId(),
              axis.measurementSetId(),
              axis.recordId(),
              axis.measuredAt(),
              axis.source(),
              axis.confidence(),
              axis.validity(),
              stale
                  ? MeasurementFreshnessStatus.STALE
                  : MeasurementFreshnessStatus.CURRENT,
              stale
          );
        })
        .toList();
  }

  private static boolean isPlanningFreshnessEvidence(
      @NonNull FeedstockProfileAxisEvidence evidence
  ) {
    return evidence.characteristic() == MeasurementCharacteristic.SPECIFIC_GRAVITY
        || evidence.characteristic() == MeasurementCharacteristic.TITRATABLE_ACIDITY;
  }

  public @NonNull FeedstockPlanningReadinessView planningReadinessWithoutMeasurement(
      @NonNull FeedstockBatchSummary batch
  ) {
    final var checks = new ArrayList<FeedstockPlanningReadinessCheck>();
    checks.add(planningCheck(
        FeedstockPlanningReadinessFactor.CURRENT_MEASUREMENT,
        FeedstockCurrentProfileSignalSeverity.BLOCKING,
        FeedstockPlanningReadinessCheckCode.CURRENT_MEASUREMENT_MISSING,
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
        FeedstockPlanningReadinessCheckCode.CURRENT_MEASUREMENT_PRESENT,
        "Current measurement is available"
    ));
    checks.add(volumeCheck(batch));
    checks.add(requiredProfileCheck(
        profile.sugar().isPresent(),
        FeedstockPlanningReadinessFactor.SPECIFIC_GRAVITY,
        FeedstockPlanningReadinessCheckCode.SUGAR_PRESENT,
        FeedstockPlanningReadinessCheckCode.SUGAR_MISSING,
        "Sugar is available",
        "Sugar is required for composition planning"
    ));
    checks.add(requiredProfileCheck(
        profile.titratableAcidity().isPresent(),
        FeedstockPlanningReadinessFactor.TITRATABLE_ACIDITY,
        FeedstockPlanningReadinessCheckCode.TITRATABLE_ACIDITY_PRESENT,
        FeedstockPlanningReadinessCheckCode.TITRATABLE_ACIDITY_MISSING,
        "Titratable acidity is available",
        "Titratable acidity is required for composition planning"
    ));
    checks.add(planningCheck(
        FeedstockPlanningReadinessFactor.TANNIN,
        profile.tannin().isPresent()
            ? FeedstockCurrentProfileSignalSeverity.OK
            : FeedstockCurrentProfileSignalSeverity.WARNING,
        profile.tannin().isPresent()
            ? FeedstockPlanningReadinessCheckCode.TANNIN_PRESENT
            : FeedstockPlanningReadinessCheckCode.TANNIN_MISSING,
        profile.tannin().isPresent()
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
      @NonNull FeedstockCurrentMeasurementFreshnessView freshness,
      @NonNull List<@NonNull FeedstockProfileAxisEvidence> evidence
  ) {
    requireNonNull(profile, "chemical profile cannot be null");
    requireNonNull(evaluation, "sugar-acidity profile evaluation cannot be null");
    requireNonNull(freshness, "feedstock measurement freshness cannot be null");
    requireNonNull(evidence, "feedstock profile evidence cannot be null");

    final var indicators = new ArrayList<FeedstockCurrentProfileDataQualityIndicator>();
    addSugarAcidityQuality(indicators, evaluation);
    addOptionalAxisQuality(indicators, profile);
    addSourceQuality(indicators, evidence);
    addConfidenceQuality(indicators, evidence);
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
    if (profile.tannin().isEmpty()) {
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
      @NonNull List<@NonNull FeedstockProfileAxisEvidence> evidence
  ) {
    if (evidence.stream().anyMatch(axis -> axis.source() == null)) {
      indicators.add(dataQualityIndicator(
          FeedstockCurrentProfileDataQualityIndicatorCode.SOURCE_MISSING,
          FeedstockCurrentProfileSignalSeverity.WARNING,
          "Measurement source is missing"
      ));
    }
  }

  private static void addConfidenceQuality(
      @NonNull List<FeedstockCurrentProfileDataQualityIndicator> indicators,
      @NonNull List<@NonNull FeedstockProfileAxisEvidence> evidence
  ) {
    if (evidence.stream().anyMatch(axis -> axis.confidence() == null)) {
      indicators.add(dataQualityIndicator(
          FeedstockCurrentProfileDataQualityIndicatorCode.CONFIDENCE_MISSING,
          FeedstockCurrentProfileSignalSeverity.WARNING,
          "Measurement confidence is missing"
      ));
      return;
    }
    if (evidence.stream().anyMatch(axis -> axis.confidence() == ConfidenceLevel.LOW)) {
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
          "Current profile contains interval values"
      ));
    }
  }

  private static boolean hasNonPointValues(@NonNull ChemicalProfile profile) {
    return profile.sugar().map(FeedstockCurrentProfileAssessmentService::isNonPoint).orElse(false)
        || profile.ph().map(FeedstockCurrentProfileAssessmentService::isNonPoint).orElse(false)
        || profile.titratableAcidity().map(FeedstockCurrentProfileAssessmentService::isNonPoint).orElse(false)
        || profile.tannin().map(FeedstockCurrentProfileAssessmentService::isNonPoint).orElse(false)
        || profile.yeastAssimilableNitrogen()
            .map(FeedstockCurrentProfileAssessmentService::isNonPoint)
            .orElse(false);
  }

  private static <T extends Comparable<? super T>> boolean isNonPoint(
      @NonNull CharacteristicValue<T> measurement
  ) {
    return switch (measurement) {
      case PointCharacteristicValue<T> _ -> false;
      case IntervalCharacteristicValue<T> _ -> true;
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
        batch.availableVolume().isZero()
            ? FeedstockPlanningReadinessCheckCode.AVAILABLE_VOLUME_EMPTY
            : FeedstockPlanningReadinessCheckCode.AVAILABLE_VOLUME_PRESENT,
        batch.availableVolume().isZero()
            ? "Available volume is zero"
            : "Available volume is present"
    );
  }

  private static @NonNull FeedstockPlanningReadinessCheck requiredProfileCheck(
      boolean present,
      @NonNull FeedstockPlanningReadinessFactor factor,
      @NonNull FeedstockPlanningReadinessCheckCode presentCode,
      @NonNull FeedstockPlanningReadinessCheckCode missingCode,
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
        planningReadinessCode(guidance.code()),
        guidance.message()
    );
  }

  private static @NonNull FeedstockPlanningReadinessCheckCode planningReadinessCode(
      @NonNull PhPlanningGuidanceCode code
  ) {
    return switch (requireNonNull(code, "pH planning guidance code cannot be null")) {
      case PH_PLANNING_IDEAL -> FeedstockPlanningReadinessCheckCode.PH_PLANNING_IDEAL;
      case PH_PLANNING_LOW -> FeedstockPlanningReadinessCheckCode.PH_PLANNING_LOW;
      case PH_PLANNING_HIGH -> FeedstockPlanningReadinessCheckCode.PH_PLANNING_HIGH;
      case PH_MISSING -> FeedstockPlanningReadinessCheckCode.PH_MISSING;
    };
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
      @NonNull FeedstockPlanningReadinessCheckCode code,
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
