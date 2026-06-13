package org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.out;

import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record FeedstockMeasurementSugarAcidityPointCandidateCursor(
    @NonNull Instant measuredAt,
    @NonNull UUID measurementId
) {

  public FeedstockMeasurementSugarAcidityPointCandidateCursor {
    requireNonNull(measuredAt, "feedstock measurement point candidate cursor measuredAt cannot be null");
    requireNonNull(measurementId, "feedstock measurement point candidate cursor id cannot be null");
  }
}
