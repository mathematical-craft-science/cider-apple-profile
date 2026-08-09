package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.mapper;

import org.craft.mcs.cap.core.feedstock.application.batch.port.in.CreateFeedstockBatchCommand;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.FeedstockBatchCreated;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchView;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.UpdateFeedstockBatchCommand;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSource;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.dto.CreateFeedstockBatchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.dto.FeedstockBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.dto.FeedstockSeasonResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.dto.PatchFeedstockBatchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.dto.UpdateFeedstockBatchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.vessel.mapper.VesselRestMapper;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper.FeedstockCriteriaRestMapper.toSeason;


public final class FeedstockBatchRestMapper {

  private static final @NonNull String WORKSPACE_ID_REQUIRED = "workspace id cannot be null";

  private FeedstockBatchRestMapper() {
  }

  public static @NonNull CreateFeedstockBatchCommand toCommand(
      @NonNull CreateFeedstockBatchRequest request
  ) {
    requireNonNull(request, "create feedstock batch request cannot be null");
    requireNonNull(request.season(), "feedstock season cannot be null");

    return new CreateFeedstockBatchCommand(
        WorkspaceId.of(requireNonNull(request.workspaceId(), WORKSPACE_ID_REQUIRED)),
        BatchName.of(request.name()),
        VesselId.of(requireNonNull(request.vesselId(), "vessel id cannot be null")),
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

    return toResponse(batch.batch());
  }

  public static @NonNull FeedstockBatchResponse toResponse(
      @NonNull FeedstockBatchView view
  ) {
    requireNonNull(view, "feedstock batch view cannot be null");
    final var batch = view.batch();

    return new FeedstockBatchResponse(
        batch.batchId().value(),
        batch.workspaceId().value(),
        batch.name().value(),
        VesselRestMapper.toResponse(view.vessel()),
        batch.type(),
        toResponse(batch.season()),
        toResponse(batch.source()),
        batch.lifecycle(),
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
        WorkspaceId.of(requireNonNull(request.workspaceId(), WORKSPACE_ID_REQUIRED)),
        BatchName.of(request.name()),
        requireNonNull(request.type(), "feedstock type cannot be null"),
        FeedstockSeason.of(request.season().startsOn(), request.season().endsOn()),
        sourceOf(request.source()),
        isBlank(request.source()),
        request.note(),
        isBlank(request.note())
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
        WorkspaceId.of(requireNonNull(request.workspaceId(), WORKSPACE_ID_REQUIRED)),
        nameOf(request.name()),
        request.type(),
        toSeason(request.season()),
        sourceOf(request.source()),
        isPresentBlank(request.source()),
        request.note(),
        isPresentBlank(request.note())
    );
  }

  private static @NonNull FeedstockSeasonResponse toResponse(@NonNull FeedstockSeason season) {
    return new FeedstockSeasonResponse(season.startsOn(), season.endsOn());
  }

  private static @Nullable String toResponse(@Nullable FeedstockSource source) {
    return source == null ? null : source.value();
  }

  private static @Nullable BatchName nameOf(@Nullable String name) {
    return name == null ? null : BatchName.of(name);
  }

  private static @Nullable FeedstockSource sourceOf(@Nullable String source) {
    return isBlank(source) ? null : FeedstockSource.of(source);
  }

  private static boolean isBlank(@Nullable String value) {
    return value == null || value.isBlank();
  }

  private static boolean isPresentBlank(@Nullable String value) {
    return value != null && value.isBlank();
  }
}
