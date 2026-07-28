package org.craft.mcs.cap.core.feedstock.application.currentprofile.model;

import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Compact state-aware current Feedstock profile with its latest contributing measurement anchor.
 */
public record FeedstockCurrentProfileSummaryView(
    @NonNull FeedstockLatestMeasurementView latestMeasurement,
    @NonNull ChemicalProfile chemicalProfile
) {

  public FeedstockCurrentProfileSummaryView {
    requireNonNull(latestMeasurement, "latest feedstock measurement cannot be null");
    requireNonNull(chemicalProfile, "chemical profile cannot be null");
  }
}
