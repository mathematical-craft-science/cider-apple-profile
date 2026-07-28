package org.craft.mcs.cap.core.feedstock.application.currentprofile.model;

import java.util.List;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceView;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * State-aware composite current profile of one Feedstock batch.
 *
 * <p>Characteristics may originate from different measurement sets along the current material
 * lineage. {@link #latestMeasurement()} is only the latest contributing measurement metadata;
 * {@link #evidence()} remains the authority for each characteristic.
 */
public record FeedstockCurrentProfileView(
    @NonNull FeedstockBatchId batchId,
    @NonNull MaterialStateId currentMaterialStateId,
    @NonNull ChemicalProfile chemicalProfile,
    @NonNull List<@NonNull MeasurementEvidenceView> evidence,
    @NonNull FeedstockLatestMeasurementView latestMeasurement
) {

  public FeedstockCurrentProfileView {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(currentMaterialStateId, "feedstock current material state id cannot be null");
    requireNonNull(chemicalProfile, "feedstock current chemical profile cannot be null");
    evidence = List.copyOf(requireNonNull(evidence, "feedstock current profile evidence cannot be null"));
    requireNonNull(latestMeasurement, "latest feedstock measurement cannot be null");
  }

  /**
   * Returns the weakest declared confidence across selected characteristics.
   *
   * @return weakest confidence, or {@code null} when evidence is absent or any item has no
   *     declared confidence
   */
  public @Nullable ConfidenceLevel aggregateConfidence() {
    if (evidence.isEmpty()) {
      return null;
    }

    var weakest = ConfidenceLevel.LAB_VERIFIED;
    for (final var item : evidence) {
      final var confidence = item.provenance().confidence();
      if (confidence == null) {
        return null;
      }
      if (confidence.compareTo(weakest) < 0) {
        weakest = confidence;
      }
    }
    return weakest;
  }

  /**
   * Returns the compact current-profile projection shared by feedstock read scenarios.
   */
  public @NonNull FeedstockCurrentProfileSummaryView summary() {
    return new FeedstockCurrentProfileSummaryView(latestMeasurement, chemicalProfile);
  }
}
