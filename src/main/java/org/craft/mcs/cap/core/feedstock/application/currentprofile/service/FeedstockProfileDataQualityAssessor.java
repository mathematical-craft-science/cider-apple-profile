package org.craft.mcs.cap.core.feedstock.application.currentprofile.service;

import java.util.ArrayList;
import java.util.List;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileFreshnessView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileDataQualityIndicator;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileDataQualityIndicatorCode;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileDataQualityStatus;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileDataQualityView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileSignalSeverity;
import org.craft.mcs.cap.core.profileanalysis.compatibility.application.ChemicalProfileAssessment;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceView;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Interprets completeness, provenance, confidence and freshness of a current Feedstock profile.
 */
@Component
public final class FeedstockProfileDataQualityAssessor {

  public @NonNull FeedstockCurrentProfileDataQualityView assessWithoutCurrentProfile() {
    final var indicators = List.of(indicator(
        FeedstockCurrentProfileDataQualityIndicatorCode.CURRENT_PROFILE_MISSING,
        FeedstockCurrentProfileSignalSeverity.BLOCKING,
        "Batch has no current profile evidence"
    ));
    return new FeedstockCurrentProfileDataQualityView(
        FeedstockCurrentProfileDataQualityStatus.BLOCKED,
        indicators
    );
  }

  public @NonNull FeedstockCurrentProfileDataQualityView assess(
      @NonNull ChemicalProfile profile,
      @NonNull ChemicalProfileAssessment evaluation,
      @NonNull FeedstockCurrentProfileFreshnessView freshness,
      @NonNull List<@NonNull MeasurementEvidenceView> evidence
  ) {
    requireNonNull(profile, "chemical profile cannot be null");
    requireNonNull(evaluation, "sugar-acidity profile evaluation cannot be null");
    requireNonNull(freshness, "feedstock measurement freshness cannot be null");
    requireNonNull(evidence, "feedstock profile evidence cannot be null");

    final var indicators = new ArrayList<FeedstockCurrentProfileDataQualityIndicator>();
    addGraphQuality(indicators, evaluation);
    addOptionalCharacteristicQuality(indicators, profile);
    addConfidenceQuality(indicators, evidence);
    addValueShapeQuality(indicators, profile);
    if (freshness.stale()) {
      indicators.add(indicator(
          FeedstockCurrentProfileDataQualityIndicatorCode.MEASUREMENT_STALE,
          FeedstockCurrentProfileSignalSeverity.WARNING,
          "Current profile contains stale evidence"
      ));
    }
    return new FeedstockCurrentProfileDataQualityView(status(indicators), indicators);
  }

  private static void addGraphQuality(
      @NonNull List<FeedstockCurrentProfileDataQualityIndicator> indicators,
      @NonNull ChemicalProfileAssessment evaluation
  ) {
    if (evaluation.plottable()) {
      indicators.add(indicator(
          FeedstockCurrentProfileDataQualityIndicatorCode.SUGAR_ACIDITY_GRAPH_PLOTTABLE,
          FeedstockCurrentProfileSignalSeverity.INFO,
          "Sugar-acidity point can be plotted from current SG and TA"
      ));
      return;
    }
    if (evaluation.capabilityAssessment().missingAxes().contains(ChemicalAxis.SPECIFIC_GRAVITY)) {
      indicators.add(indicator(
          FeedstockCurrentProfileDataQualityIndicatorCode.SPECIFIC_GRAVITY_MISSING,
          FeedstockCurrentProfileSignalSeverity.BLOCKING,
          "Specific gravity is missing"
      ));
    }
    if (evaluation.capabilityAssessment().missingAxes().contains(ChemicalAxis.TITRATABLE_ACIDITY)) {
      indicators.add(indicator(
          FeedstockCurrentProfileDataQualityIndicatorCode.TITRATABLE_ACIDITY_MISSING,
          FeedstockCurrentProfileSignalSeverity.BLOCKING,
          "Titratable acidity is missing"
      ));
    }
  }

  private static void addOptionalCharacteristicQuality(
      @NonNull List<FeedstockCurrentProfileDataQualityIndicator> indicators,
      @NonNull ChemicalProfile profile
  ) {
    if (profile.tannin().isEmpty()) {
      indicators.add(indicator(
          FeedstockCurrentProfileDataQualityIndicatorCode.TANNIN_MISSING,
          FeedstockCurrentProfileSignalSeverity.WARNING,
          "Tannin is missing"
      ));
    }
    if (profile.ph().isEmpty()) {
      indicators.add(indicator(
          FeedstockCurrentProfileDataQualityIndicatorCode.PH_MISSING,
          FeedstockCurrentProfileSignalSeverity.WARNING,
          "pH is missing"
      ));
    }
    if (profile.yeastAssimilableNitrogen().isEmpty()) {
      indicators.add(indicator(
          FeedstockCurrentProfileDataQualityIndicatorCode.YAN_MISSING,
          FeedstockCurrentProfileSignalSeverity.WARNING,
          "YAN is missing"
      ));
    }
  }

  private static void addConfidenceQuality(
      @NonNull List<FeedstockCurrentProfileDataQualityIndicator> indicators,
      @NonNull List<@NonNull MeasurementEvidenceView> evidence
  ) {
    if (evidence.stream().anyMatch(snapshot -> snapshot.provenance().confidence() == null)) {
      indicators.add(indicator(
          FeedstockCurrentProfileDataQualityIndicatorCode.CONFIDENCE_MISSING,
          FeedstockCurrentProfileSignalSeverity.WARNING,
          "Measurement confidence is missing"
      ));
      return;
    }
    if (evidence.stream().anyMatch(snapshot -> snapshot.provenance().confidence() == ConfidenceLevel.LOW)) {
      indicators.add(indicator(
          FeedstockCurrentProfileDataQualityIndicatorCode.LOW_CONFIDENCE,
          FeedstockCurrentProfileSignalSeverity.WARNING,
          "Measurement confidence is low"
      ));
    }
  }

  private static void addValueShapeQuality(
      @NonNull List<FeedstockCurrentProfileDataQualityIndicator> indicators,
      @NonNull ChemicalProfile profile
  ) {
    if (hasIntervalValues(profile)) {
      indicators.add(indicator(
          FeedstockCurrentProfileDataQualityIndicatorCode.NON_POINT_VALUES,
          FeedstockCurrentProfileSignalSeverity.INFO,
          "Current profile contains interval values"
      ));
    }
  }

  private static boolean hasIntervalValues(@NonNull ChemicalProfile profile) {
    return profile.sugar().map(FeedstockProfileDataQualityAssessor::isInterval).orElse(false)
        || profile.ph().map(FeedstockProfileDataQualityAssessor::isInterval).orElse(false)
        || profile.titratableAcidity()
            .map(FeedstockProfileDataQualityAssessor::isInterval)
            .orElse(false)
        || profile.tannin().map(FeedstockProfileDataQualityAssessor::isInterval).orElse(false)
        || profile.yeastAssimilableNitrogen()
            .map(FeedstockProfileDataQualityAssessor::isInterval)
            .orElse(false);
  }

  private static <T extends Comparable<? super T>> boolean isInterval(
      @NonNull CharacteristicValue<T> value
  ) {
    return switch (value) {
      case PointCharacteristicValue<T> _ -> false;
      case IntervalCharacteristicValue<T> _ -> true;
    };
  }

  private static @NonNull FeedstockCurrentProfileDataQualityIndicator indicator(
      @NonNull FeedstockCurrentProfileDataQualityIndicatorCode code,
      @NonNull FeedstockCurrentProfileSignalSeverity severity,
      @NonNull String message
  ) {
    return new FeedstockCurrentProfileDataQualityIndicator(code, severity, message);
  }

  private static @NonNull FeedstockCurrentProfileDataQualityStatus status(
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
