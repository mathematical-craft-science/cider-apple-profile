package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record CustomRectangleFeedstockSampleBlendProfileTargetRequest(
    @NonNull String kind,
    @NonNull BigDecimal sugarMin,
    @NonNull BigDecimal sugarMax,
    @NonNull BigDecimal acidityMin,
    @NonNull BigDecimal acidityMax
) implements FeedstockSampleBlendProfileTargetRequest {

  public CustomRectangleFeedstockSampleBlendProfileTargetRequest {
    requireNonNull(kind, "target kind cannot be null");
    requireNonNull(sugarMin, "target sugar minimum cannot be null");
    requireNonNull(sugarMax, "target sugar maximum cannot be null");
    requireNonNull(acidityMin, "target acidity minimum cannot be null");
    requireNonNull(acidityMax, "target acidity maximum cannot be null");
  }
}
