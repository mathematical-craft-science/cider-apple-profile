package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.mapper;

import org.craft.mcs.cap.core.feedstock.application.measurement.port.in.CreateFeedstockMeasurementCommand;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.in.FeedstockMeasurementCreated;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementSummary;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.in.UpdateFeedstockMeasurementCommand;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.ChemicalProfileRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.CreateFeedstockMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.FeedstockMeasurementResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.PatchFeedstockMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.UpdateFeedstockMeasurementRequest;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public final class FeedstockMeasurementRestMapper {

  private static final @NonNull String FEEDSTOCK_BATCH_ID_REQUIRED = "feedstock batch id cannot be null";

  private FeedstockMeasurementRestMapper() {
  }

  public static @NonNull CreateFeedstockMeasurementCommand toCommand(
      @NonNull FeedstockBatchId batchId,
      @NonNull CreateFeedstockMeasurementRequest request
  ) {
    requireNonNull(batchId, FEEDSTOCK_BATCH_ID_REQUIRED);
    requireNonNull(request, "create feedstock measurement request cannot be null");

    return new CreateFeedstockMeasurementCommand(
        batchId,
        FeedstockMeasurementName.of(request.name()),
        ChemicalProfileRestMapper.toDomain(
            requireNonNull(request.chemicalProfile(), "chemical profile cannot be null")
        ),
        requireNonNull(request.measuredAt(), "measured at cannot be null"),
        request.note()
    );
  }

  public static @NonNull FeedstockMeasurementResponse toResponse(@NonNull FeedstockMeasurementCreated measurement) {
    requireNonNull(measurement, "created feedstock measurement cannot be null");

    return new FeedstockMeasurementResponse(
        measurement.measurementId().value(),
        measurement.workspaceId().value(),
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
        measurement.workspaceId().value(),
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
    requireNonNull(batchId, FEEDSTOCK_BATCH_ID_REQUIRED);
    requireNonNull(measurementId, "feedstock measurement id cannot be null");
    requireNonNull(request, "update feedstock measurement request cannot be null");

    return new UpdateFeedstockMeasurementCommand(
        batchId,
        measurementId,
        FeedstockMeasurementName.of(request.name()),
        ChemicalProfileRestMapper.toDomain(
            requireNonNull(request.chemicalProfile(), "chemical profile cannot be null")
        ),
        false,
        requireNonNull(request.measuredAt(), "measured at cannot be null"),
        request.note(),
        isBlank(request.note())
    );
  }

  public static @NonNull UpdateFeedstockMeasurementCommand toCommand(
      @NonNull FeedstockBatchId batchId,
      @NonNull FeedstockMeasurementId measurementId,
      @NonNull PatchFeedstockMeasurementRequest request
  ) {
    requireNonNull(batchId, FEEDSTOCK_BATCH_ID_REQUIRED);
    requireNonNull(measurementId, "feedstock measurement id cannot be null");
    requireNonNull(request, "patch feedstock measurement request cannot be null");

    return new UpdateFeedstockMeasurementCommand(
        batchId,
        measurementId,
        nameOf(request.name()),
        chemicalProfileOf(request),
        true,
        request.measuredAt(),
        request.note(),
        isPresentBlank(request.note())
    );
  }

  private static @Nullable FeedstockMeasurementName nameOf(@Nullable String name) {
    return name == null ? null : FeedstockMeasurementName.of(name);
  }

  private static @Nullable ChemicalProfile chemicalProfileOf(@NonNull PatchFeedstockMeasurementRequest request) {
    return request.chemicalProfile() == null
           ? null
           : ChemicalProfileRestMapper.toDomain(request.chemicalProfile()); // NOSONAR
  }

  private static boolean isBlank(@Nullable String value) {
    return value == null || value.isBlank();
  }

  private static boolean isPresentBlank(@Nullable String value) {
    return value != null && value.isBlank();
  }
}
