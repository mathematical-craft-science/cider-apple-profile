package org.craft.mcs.cap.shared.testing;

import java.time.Instant;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/** Immutable MeasurementSet fixture used to build Feedstock current-profile test projections. */
public record FeedstockMeasurementFixture(
    @NonNull MeasurementSetId measurementId,
    @NonNull FeedstockBatchId batchId,
    @NonNull MeasurementSetName name,
    @NonNull ProfileCharacteristicsFixture characteristics,
    @NonNull Instant measuredAt,
    @Nullable String note
) {

  public static @NonNull FeedstockMeasurementFixture create(
      @NonNull MeasurementSetId measurementId,
      @NonNull FeedstockBatchId batchId,
      @NonNull MeasurementSetName name,
      @NonNull ProfileCharacteristicsFixture characteristics,
      @NonNull Instant measuredAt,
      @NonNull Instant recordedAt
  ) {
    return new FeedstockMeasurementFixture(
        measurementId,
        batchId,
        name,
        characteristics,
        measuredAt,
        null
    );
  }

  public @NonNull FeedstockMeasurementFixture changeNote(@Nullable String note) {
    return new FeedstockMeasurementFixture(
        measurementId,
        batchId,
        name,
        characteristics,
        measuredAt,
        note
    );
  }
}
