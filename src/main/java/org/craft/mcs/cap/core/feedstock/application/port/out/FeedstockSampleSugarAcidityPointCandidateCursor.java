package org.craft.mcs.cap.core.feedstock.application.port.out;

import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record FeedstockSampleSugarAcidityPointCandidateCursor(
    @NonNull Instant measuredAt,
    @NonNull UUID sampleId
) {

  public FeedstockSampleSugarAcidityPointCandidateCursor {
    requireNonNull(measuredAt, "feedstock sample point candidate cursor measuredAt cannot be null");
    requireNonNull(sampleId, "feedstock sample point candidate cursor id cannot be null");
  }
}
