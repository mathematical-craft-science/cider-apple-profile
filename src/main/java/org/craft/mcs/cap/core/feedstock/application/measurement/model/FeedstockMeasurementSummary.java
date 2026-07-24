package org.craft.mcs.cap.core.feedstock.application.measurement.model;

import java.time.Instant;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record FeedstockMeasurementSummary(
    @NonNull FeedstockMeasurementId measurementId,
    @NonNull WorkspaceId workspaceId,
    @NonNull FeedstockBatchId batchId,
    @NonNull FeedstockMeasurementName name,
    @NonNull Instant measuredAt,
    @NonNull ChemicalProfile chemicalProfile,
    @Nullable String note
) {

  public FeedstockMeasurementSummary {
    requireNonNull(measurementId, "feedstock measurement id cannot be null");
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(name, "feedstock measurement name cannot be null");
    requireNonNull(measuredAt, "measured at cannot be null");
    requireNonNull(chemicalProfile, "chemical profile cannot be null");
  }
}
