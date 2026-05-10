package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.mapper;

import org.craft.mcs.cap.core.feedstock.application.port.in.CreateFeedstockSampleCommand;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSampleCreated;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSampleSummary;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.FeedstockSampleName;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.ChemicalProfileRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.CreateFeedstockSampleRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockSampleResponse;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class FeedstockSampleRestMapper {

  private FeedstockSampleRestMapper() {
  }

  public static @NonNull CreateFeedstockSampleCommand toCommand(
      @NonNull CreateFeedstockSampleRequest request
  ) {
    requireNonNull(request, "create feedstock sample request cannot be null");

    return new CreateFeedstockSampleCommand(
        FeedstockBatchId.of(requireNonNull(request.batchId(), "feedstock batch id cannot be null")),
        FeedstockSampleName.of(request.name()),
        requireNonNull(request.type(), "feedstock type cannot be null"),
        ChemicalProfileRestMapper.toDomain(request.chemicalProfile()),
        requireNonNull(request.measuredAt(), "measured at cannot be null"),
        request.note()
    );
  }

  public static @NonNull FeedstockSampleResponse toResponse(@NonNull FeedstockSampleCreated sample) {
    requireNonNull(sample, "created feedstock sample cannot be null");

    return new FeedstockSampleResponse(
        sample.sampleId().value(),
        sample.ciderMakerId().value(),
        sample.batchId().value(),
        sample.name().value(),
        sample.type(),
        null,
        null
    );
  }

  public static @NonNull FeedstockSampleResponse toResponse(@NonNull FeedstockSampleSummary sample) {
    requireNonNull(sample, "feedstock sample summary cannot be null");

    return new FeedstockSampleResponse(
        sample.sampleId().value(),
        sample.ciderMakerId().value(),
        sample.batchId().value(),
        sample.name().value(),
        sample.type(),
        sample.measuredAt(),
        sample.note()
    );
  }
}
