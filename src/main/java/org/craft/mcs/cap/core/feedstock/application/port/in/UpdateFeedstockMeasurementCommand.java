package org.craft.mcs.cap.core.feedstock.application.port.in;

import java.time.Instant;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record UpdateFeedstockMeasurementCommand(
    @NonNull FeedstockBatchId batchId,
    @NonNull FeedstockMeasurementId measurementId,
    @Nullable FeedstockMeasurementName name,
    @Nullable ChemicalProfile chemicalProfile,
    boolean mergeChemicalProfile,
    @Nullable Instant measuredAt,
    @Nullable String note,
    boolean clearNote
) {

  public UpdateFeedstockMeasurementCommand {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(measurementId, "feedstock measurement id cannot be null");
  }
}
