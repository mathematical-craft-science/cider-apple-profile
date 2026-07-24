package org.craft.mcs.cap.shared.testing;

import java.time.Instant;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementName;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/** Immutable fixture matching the legacy Feedstock measurement read projection used by existing tests. */
public record FeedstockMeasurementFixture(
    @NonNull FeedstockMeasurementId measurementId,
    @NonNull FeedstockBatchId batchId,
    @NonNull FeedstockMeasurementName name,
    @NonNull ChemicalProfile chemicalProfile,
    @NonNull Instant measuredAt,
    @Nullable String note
) {

  public static @NonNull FeedstockMeasurementFixture create(
      @NonNull FeedstockMeasurementId measurementId,
      @NonNull FeedstockBatchId batchId,
      @NonNull FeedstockMeasurementName name,
      @NonNull ChemicalProfile chemicalProfile,
      @NonNull Instant measuredAt,
      @NonNull Instant recordedAt
  ) {
    return new FeedstockMeasurementFixture(
        measurementId,
        batchId,
        name,
        chemicalProfile,
        measuredAt,
        null
    );
  }

  public @NonNull FeedstockMeasurementFixture changeNote(@Nullable String note) {
    return new FeedstockMeasurementFixture(
        measurementId,
        batchId,
        name,
        chemicalProfile,
        measuredAt,
        note
    );
  }
}
