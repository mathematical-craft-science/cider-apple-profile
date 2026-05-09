package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.mapper;

import org.craft.mcs.cap.core.feedstock.application.port.in.CreateFeedstockBatchCommand;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchCreated;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockBatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSource;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.CreateFeedstockBatchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockSeasonResponse;
import org.craft.mcs.cap.shared.domain.measurement.VolumeValue;
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
        batch.batchId().value().toString(),
        batch.ciderMakerId().value().toString(),
        batch.name().value(),
        toResponse(batch.season()),
        batch.source() == null ? null : batch.source().value(), // NOSONAR
        batch.availableVolume().liters(),
        batch.note()
    );
  }

  public static @NonNull FeedstockBatchResponse toResponse(
      @NonNull FeedstockBatchSummary batch
  ) {
    requireNonNull(batch, "feedstock batch summary cannot be null");

    return new FeedstockBatchResponse(
        batch.batchId().value().toString(),
        batch.ciderMakerId().value().toString(),
        batch.name().value(),
        toResponse(batch.season()),
        batch.source() == null ? null : batch.source().value(), // NOSONAR
        batch.availableVolume().liters(),
        batch.note()
    );
  }

  private static @NonNull FeedstockSeasonResponse toResponse(@NonNull FeedstockSeason season) {
    return new FeedstockSeasonResponse(season.startsOn(), season.endsOn());
  }

  private static @Nullable FeedstockSource sourceOf(@Nullable String source) {
    return source == null || source.isBlank() ? null : FeedstockSource.of(source);
  }
}
