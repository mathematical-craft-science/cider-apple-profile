package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.mapper;

import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSamplePotentialAbvPrediction;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockSamplePotentialAbvResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.fermentation.mapper.PotentialAbvPredictionRestMapper;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class FeedstockSamplePotentialAbvRestMapper {

  private FeedstockSamplePotentialAbvRestMapper() {
  }

  public static @NonNull FeedstockSamplePotentialAbvResponse toResponse(
      @NonNull FeedstockSamplePotentialAbvPrediction prediction
  ) {
    requireNonNull(prediction, "feedstock sample potential ABV prediction cannot be null");

    return new FeedstockSamplePotentialAbvResponse(
        prediction.sampleId().value().toString(),
        prediction.ciderMakerId().value().toString(),
        prediction.batchId().value().toString(),
        prediction.sampleName().value(),
        prediction.originalGravityValueKind().name(),
        PotentialAbvPredictionRestMapper.toResponse(prediction.prediction())
    );
  }
}
