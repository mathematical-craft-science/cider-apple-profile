package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.mapper;

import org.craft.mcs.cap.core.feedstock.application.port.in.CreateFeedstockMeasurementCommand;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockMeasurementCreated;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockMeasurementSummary;
import org.craft.mcs.cap.core.feedstock.application.port.in.UpdateFeedstockMeasurementCommand;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.ChemicalProfileRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.CreateFeedstockMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockMeasurementResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.PatchFeedstockMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.UpdateFeedstockMeasurementRequest;
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
        measurement.measuredAt(),
        ChemicalProfileRestMapper.toResponse(measurement.chemicalProfile()),
        measurement.note()
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
        ChemicalProfileRestMapper.toResponse(measurement.chemicalProfile()),
        measurement.note()
    );
  }

  public static @NonNull UpdateFeedstockMeasurementCommand toCommand(
      @NonNull FeedstockBatchId batchId,
      @NonNull FeedstockMeasurementId measurementId,
      @NonNull UpdateFeedstockMeasurementRequest request
  ) {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(measurementId, "feedstock measurement id cannot be null");
    requireNonNull(request, "update feedstock measurement request cannot be null");

    return new UpdateFeedstockMeasurementCommand(
        batchId,
        measurementId,
        FeedstockMeasurementName.of(request.name()),
        ChemicalProfileRestMapper.toDomain(request.chemicalProfile()),
        false,
        requireNonNull(request.measuredAt(), "measured at cannot be null"),
        request.note(),
        request.note() == null || request.note().isBlank()  // NOSONAR
    );
  }

  public static @NonNull UpdateFeedstockMeasurementCommand toCommand(
      @NonNull FeedstockBatchId batchId,
      @NonNull FeedstockMeasurementId measurementId,
      @NonNull PatchFeedstockMeasurementRequest request
  ) {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(measurementId, "feedstock measurement id cannot be null");
    requireNonNull(request, "patch feedstock measurement request cannot be null");

    return new UpdateFeedstockMeasurementCommand(
        batchId,
        measurementId,
        request.name() == null ? null : FeedstockMeasurementName.of(request.name()),  // NOSONAR
        request.chemicalProfile() == null ? null : ChemicalProfileRestMapper.toDomain(request.chemicalProfile()),  // NOSONAR
        true,
        request.measuredAt(),
        request.note(),
        request.note() != null && request.note().isBlank()  // NOSONAR
    );
  }
}
