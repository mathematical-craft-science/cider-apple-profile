package org.craft.mcs.cap.core.feedstock.application.currentprofile.service;

import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileFreshnessView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileView;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceView;
import org.craft.mcs.cap.shared.application.measurement.MeasurementFreshnessStatus;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Aggregates authoritative per-characteristic freshness for Feedstock planning coordinates.
 */
@Component
public final class FeedstockCurrentProfileFreshnessAssessor {

  private static final @NonNull String STALE_MEASUREMENT_REASON =
      "Current profile contains planning evidence older than the configured freshness threshold";

  public @NonNull FeedstockCurrentProfileFreshnessView assess(
      @NonNull FeedstockCurrentProfileView currentProfile
  ) {
    requireNonNull(currentProfile, "feedstock current profile cannot be null");
    final var planningEvidenceIsStale = currentProfile.evidence().stream()
        .filter(FeedstockCurrentProfileFreshnessAssessor::isPlanningEvidence)
        .anyMatch(evidence -> evidence.freshnessStatus() == MeasurementFreshnessStatus.STALE);
    return planningEvidenceIsStale
        ? new FeedstockCurrentProfileFreshnessView(
            MeasurementFreshnessStatus.STALE,
            STALE_MEASUREMENT_REASON
        )
        : new FeedstockCurrentProfileFreshnessView(MeasurementFreshnessStatus.CURRENT, null);
  }

  private static boolean isPlanningEvidence(
      @NonNull MeasurementEvidenceView evidence
  ) {
    return evidence.characteristic() == MeasurementCharacteristic.SPECIFIC_GRAVITY
        || evidence.characteristic() == MeasurementCharacteristic.TITRATABLE_ACIDITY;
  }
}
