package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.mapper;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.CreateFeedstockBatchCommand;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.FeedstockBatchCreated;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.UpdateFeedstockBatchCommand;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockBatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSource;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockVessel;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.dto.CreateFeedstockBatchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.dto.FeedstockBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.dto.FeedstockSeasonResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.dto.FeedstockSeasonRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.dto.PatchFeedstockBatchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.dto.UpdateFeedstockBatchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


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
        FeedstockBatchName.of(request.name()),
        vesselOf(request.vessel()),
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
        batch.workspaceId().value(),
        batch.name().value(),
        toResponse(batch.vessel()),
        batch.type(),
        toResponse(batch.season()),
        toResponse(batch.source()),
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
        batch.workspaceId().value(),
        batch.name().value(),
        toResponse(batch.vessel()),
        batch.type(),
        toResponse(batch.season()),
        toResponse(batch.source()),
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
        FeedstockBatchName.of(request.name()),
        vesselOf(request.vessel()),
        isBlank(request.vessel()),
        requireNonNull(request.type(), "feedstock type cannot be null"),
        FeedstockSeason.of(request.season().startsOn(), request.season().endsOn()),
        sourceOf(request.source()),
        isBlank(request.source()),
        VolumeValue.ofLiters(request.availableVolumeLiters()),
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
        vesselOf(request.vessel()),
        isPresentBlank(request.vessel()),
        request.type(),
        seasonOf(request.season()),
        sourceOf(request.source()),
        isPresentBlank(request.source()),
        volumeOf(request.availableVolumeLiters()),
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

  private static @Nullable String toResponse(@Nullable FeedstockVessel vessel) {
    return vessel == null ? null : vessel.value();
  }

  private static @Nullable FeedstockBatchName nameOf(@Nullable String name) {
    return name == null ? null : FeedstockBatchName.of(name);
  }

  private static @Nullable FeedstockVessel vesselOf(@Nullable String vessel) {
    return isBlank(vessel) ? null : FeedstockVessel.of(vessel);
  }

  private static @Nullable FeedstockSeason seasonOf(@Nullable FeedstockSeasonRequest request) {
    return request == null ? null : FeedstockSeason.of(request.startsOn(), request.endsOn());
  }

  private static @Nullable FeedstockSource sourceOf(@Nullable String source) {
    return isBlank(source) ? null : FeedstockSource.of(source);
  }

  private static @Nullable VolumeValue volumeOf(@Nullable BigDecimal liters) {
    return liters == null ? null : VolumeValue.ofLiters(liters);
  }

  private static boolean isBlank(@Nullable String value) {
    return value == null || value.isBlank();
  }

  private static boolean isPresentBlank(@Nullable String value) {
    return value != null && value.isBlank();
  }
}
