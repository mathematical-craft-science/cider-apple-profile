package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.mapper;

import org.craft.mcs.cap.core.feedstock.application.port.in.CreateFeedstockBatchCommand;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchCreated;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.port.in.UpdateFeedstockBatchCommand;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockBatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSource;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.CreateFeedstockBatchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockSeasonResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.PatchFeedstockBatchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.UpdateFeedstockBatchRequest;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public final class FeedstockBatchRestMapper {

  private FeedstockBatchRestMapper() {
  }

  public static @NonNull CreateFeedstockBatchCommand toCommand(
      @NonNull CreateFeedstockBatchRequest request
  ) {
    requireNonNull(request, "create feedstock batch request cannot be null");
    requireNonNull(request.season(), "feedstock season cannot be null");

    return new CreateFeedstockBatchCommand(
        CiderMakerId.of(requireNonNull(request.ciderMakerId(), "cider maker id cannot be null")),
        FeedstockBatchName.of(request.name()),
        requireNonNull(request.type(), "feedstock type cannot be null"),
        FeedstockSeason.of(request.season().startsOn(), request.season().endsOn()),
        sourceOf(request.source()),
        VolumeValue.ofLiters(request.availableVolumeLiters()),
        request.note()
    );
  }

  public static @NonNull FeedstockBatchResponse toResponse(
      @NonNull FeedstockBatchCreated batch
  ) {
    requireNonNull(batch, "created feedstock batch cannot be null");

    return new FeedstockBatchResponse(
        batch.batchId().value(),
        batch.ciderMakerId().value(),
        batch.name().value(),
        batch.type(),
        toResponse(batch.season()),
        batch.source() == null ? null : batch.source().value(), // NOSONAR
        new DecimalQuantityResponse(batch.availableVolume().liters(), CanonicalUnitLabels.VOLUME),
        batch.note()
    );
  }

  public static @NonNull FeedstockBatchResponse toResponse(
      @NonNull FeedstockBatchSummary batch
  ) {
    requireNonNull(batch, "feedstock batch summary cannot be null");

    return new FeedstockBatchResponse(
        batch.batchId().value(),
        batch.ciderMakerId().value(),
        batch.name().value(),
        batch.type(),
        toResponse(batch.season()),
        batch.source() == null ? null : batch.source().value(), // NOSONAR
        new DecimalQuantityResponse(batch.availableVolume().liters(), CanonicalUnitLabels.VOLUME),
        batch.note()
    );
  }

  public static @NonNull UpdateFeedstockBatchCommand toCommand(
      @NonNull FeedstockBatchId batchId,
      @NonNull UpdateFeedstockBatchRequest request
  ) {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(request, "update feedstock batch request cannot be null");
    requireNonNull(request.season(), "feedstock season cannot be null");

    return new UpdateFeedstockBatchCommand(
        batchId,
        CiderMakerId.of(requireNonNull(request.ciderMakerId(), "cider maker id cannot be null")),
        FeedstockBatchName.of(request.name()),
        requireNonNull(request.type(), "feedstock type cannot be null"),
        FeedstockSeason.of(request.season().startsOn(), request.season().endsOn()),
        sourceOf(request.source()),
        request.source() == null || request.source().isBlank(),  // NOSONAR
        VolumeValue.ofLiters(request.availableVolumeLiters()),
        request.note(),
        request.note() == null || request.note().isBlank()  // NOSONAR
    );
  }

  public static @NonNull UpdateFeedstockBatchCommand toCommand(
      @NonNull FeedstockBatchId batchId,
      @NonNull PatchFeedstockBatchRequest request
  ) {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(request, "patch feedstock batch request cannot be null");

    return new UpdateFeedstockBatchCommand(
        batchId,
        CiderMakerId.of(requireNonNull(request.ciderMakerId(), "cider maker id cannot be null")),
        request.name() == null ? null : FeedstockBatchName.of(request.name()), // NOSONAR
        request.type(),
        request.season() == null ? null : FeedstockSeason.of(
            request.season().startsOn(),  // NOSONAR
            request.season().endsOn()),  // NOSONAR
        sourceOf(request.source()),
        request.source() != null && request.source().isBlank(),  // NOSONAR
        request.availableVolumeLiters() == null ? null : VolumeValue.ofLiters(request.availableVolumeLiters()),  // NOSONAR
        request.note(),
        request.note() != null && request.note().isBlank()  // NOSONAR
    );
  }

  private static @NonNull FeedstockSeasonResponse toResponse(@NonNull FeedstockSeason season) {
    return new FeedstockSeasonResponse(season.startsOn(), season.endsOn());
  }

  private static @Nullable FeedstockSource sourceOf(@Nullable String source) {
    return source == null || source.isBlank() ? null : FeedstockSource.of(source);
  }
}
