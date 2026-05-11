package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.mapper;

import org.craft.mcs.cap.core.feedstock.application.port.in.CreateFeedstockMeasurementCommand;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockMeasurementCreated;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockMeasurementSummary;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.ChemicalProfileRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.CreateFeedstockMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockMeasurementResponse;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class FeedstockMeasurementRestMapper {

  private FeedstockMeasurementRestMapper() {
  }

  public static @NonNull CreateFeedstockMeasurementCommand toCommand(
      @NonNull FeedstockBatchId batchId,
      @NonNull CreateFeedstockMeasurementRequest request
  ) {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(request, "create feedstock measurement request cannot be null");

    return new CreateFeedstockMeasurementCommand(
        batchId,
        FeedstockMeasurementName.of(request.name()),
        ChemicalProfileRestMapper.toDomain(request.chemicalProfile()),
        requireNonNull(request.measuredAt(), "measured at cannot be null"),
        request.note()
    );
  }

  public static @NonNull FeedstockMeasurementResponse toResponse(@NonNull FeedstockMeasurementCreated measurement) {
    requireNonNull(measurement, "created feedstock measurement cannot be null");

    return new FeedstockMeasurementResponse(
        measurement.measurementId().value(),
        measurement.ciderMakerId().value(),
        measurement.batchId().value(),
        measurement.name().value(),
        null,
        null
    );
  }

  public static @NonNull FeedstockMeasurementResponse toResponse(@NonNull FeedstockMeasurementSummary measurement) {
    requireNonNull(measurement, "feedstock measurement summary cannot be null");

    return new FeedstockMeasurementResponse(
        measurement.measurementId().value(),
        measurement.ciderMakerId().value(),
        measurement.batchId().value(),
        measurement.name().value(),
        measurement.measuredAt(),
        measurement.note()
    );
  }
}
