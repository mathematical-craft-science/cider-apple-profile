package org.craft.mcs.cap.core.feedstock.application.measurement.model;

import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Immutable application projection of measurement evidence used by feedstock read scenarios. */
public record FeedstockMeasurementView(
    @NonNull FeedstockMeasurementId measurementId,
    @NonNull FeedstockBatchId batchId,
    @NonNull FeedstockMeasurementName name,
    @NonNull ChemicalProfile chemicalProfile,
    @NonNull Instant measuredAt,
    long evidenceVersion,
    @NonNull List<@NonNull FeedstockProfileAxisEvidence> axisEvidence,
    @Nullable String note
) {

  public FeedstockMeasurementView {
    requireNonNull(measurementId, "feedstock measurement id cannot be null");
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(name, "feedstock measurement name cannot be null");
    requireNonNull(chemicalProfile, "feedstock chemical profile cannot be null");
    requireNonNull(measuredAt, "feedstock measurement time cannot be null");
    axisEvidence = List.copyOf(requireNonNull(axisEvidence, "feedstock axis evidence cannot be null"));
    if (evidenceVersion < 0) {
      throw new ApplicationValidationException("feedstock measurement evidence version cannot be negative");
    }
  }

  public FeedstockMeasurementView(
      @NonNull FeedstockMeasurementId measurementId,
      @NonNull FeedstockBatchId batchId,
      @NonNull FeedstockMeasurementName name,
      @NonNull ChemicalProfile chemicalProfile,
      @NonNull Instant measuredAt,
      long evidenceVersion,
      @Nullable String note
  ) {
    this(
        measurementId,
        batchId,
        name,
        chemicalProfile,
        measuredAt,
        evidenceVersion,
        List.of(),
        note
    );
  }

  /**
   * Returns the weakest declared confidence across selected axes, or no aggregate confidence when
   * any selected axis has no explicit confidence.
   */
  public @Nullable ConfidenceLevel aggregateConfidence() {
    if (axisEvidence.isEmpty()) {
      return null;
    }

    var weakest = ConfidenceLevel.LAB_VERIFIED;
    for (final var evidence : axisEvidence) {
      final var confidence = evidence.confidence();
      if (confidence == null) {
        return null;
      }
      if (confidence.compareTo(weakest) < 0) {
        weakest = confidence;
      }
    }
    return weakest;
  }
}
