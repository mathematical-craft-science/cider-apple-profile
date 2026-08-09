package org.craft.mcs.cap.core.feedstock.application.batch.model;

import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.application.material.VesselReferenceView;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Feedstock batch state enriched with its current Vessel catalog reference. */
public record FeedstockBatchView(
    @NonNull FeedstockBatchSummary batch,
    @NonNull VesselReferenceView vessel
) {

  public FeedstockBatchView {
    requireNonNull(batch, "feedstock batch summary cannot be null");
    requireNonNull(vessel, "feedstock vessel reference cannot be null");
    if (!batch.vesselId().equals(vessel.vesselId())) {
      throw new ApplicationValidationException(
          "feedstock vessel reference must match batch vessel id"
      );
    }
  }
}
