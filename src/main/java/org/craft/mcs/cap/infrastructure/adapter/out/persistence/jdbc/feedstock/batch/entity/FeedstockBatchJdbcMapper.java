package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.batch.entity;

import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSource;
import org.craft.mcs.cap.shared.kernel.material.value.Vessel;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public final class FeedstockBatchJdbcMapper {

  private FeedstockBatchJdbcMapper() {
  }

  public static @NonNull FeedstockBatchJdbcEntity fromDomain(@NonNull FeedstockBatch batch) {
    requireNonNull(batch, "feedstock batch cannot be null");

    return new FeedstockBatchJdbcEntity(
        batch.batchId().value(),
        batch.workspaceId().value(),
        batch.name().value(),
        toJdbc(batch.vessel()),
        batch.type(),
        batch.season().startsOn(),
        batch.season().endsOn(),
        batch.source() == null ? null : batch.source().value(),
        batch.lifecycle(),
        batch.availableVolume().liters(),
        batch.currentMaterialStateId().value(),
        batch.note(),
        batch.deletedAt()
    );
  }

  public static @NonNull FeedstockBatchJdbcEntity updateFromDomain(
      @NonNull FeedstockBatch batch,
      @NonNull FeedstockBatchJdbcEntity entity
  ) {
    requireNonNull(batch, "feedstock batch cannot be null");
    requireNonNull(entity, "feedstock batch row cannot be null");

    entity.name = batch.name().value();
    entity.vessel = toJdbc(batch.vessel());
    entity.type = batch.type();
    entity.seasonStartsOn = batch.season().startsOn();
    entity.seasonEndsOn = batch.season().endsOn();
    entity.source = batch.source() == null ? null : batch.source().value();
    entity.lifecycle = batch.lifecycle();
    entity.availableVolumeLiters = batch.availableVolume().liters();
    entity.currentMaterialStateId = batch.currentMaterialStateId().value();
    entity.note = batch.note();
    entity.deletedAt = batch.deletedAt();

    return entity;
  }

  public static @NonNull FeedstockBatch toDomain(@NonNull FeedstockBatchJdbcEntity entity) {
    requireNonNull(entity, "feedstock batch row cannot be null");

    final var source = entity.source == null ? null : FeedstockSource.of(entity.source);

    return FeedstockBatch.restore(
        FeedstockBatchId.of(requireNonNull(entity.batchId, "feedstock batch id cannot be null")),
        WorkspaceId.of(requireNonNull(entity.workspaceId, "workspace id cannot be null")),
        BatchName.of(requireNonNull(entity.name, "feedstock batch name cannot be null")),
        entity.vessel == null ? null : Vessel.named(entity.vessel),
        requireNonNull(entity.type, "feedstock type cannot be null"),
        FeedstockSeason.of(
            requireNonNull(entity.seasonStartsOn, "feedstock season start date cannot be null"),
            requireNonNull(entity.seasonEndsOn, "feedstock season end date cannot be null")
        ),
        source,
        requireNonNull(entity.lifecycle, "feedstock batch lifecycle cannot be null"),
        VolumeValue.ofLiters(requireNonNull(entity.availableVolumeLiters, "available volume cannot be null")),
        MaterialStateId.of(requireNonNull(
            entity.currentMaterialStateId,
            "feedstock current material state id cannot be null"
        )),
        entity.note,
        entity.deletedAt
    );
  }

  public static @NonNull VersionedAggregate<FeedstockBatch> toVersionedAggregate(
      @NonNull FeedstockBatchJdbcEntity entity
  ) {
    requireNonNull(entity, "feedstock batch row cannot be null");
    return new VersionedAggregate<>(toDomain(entity), AggregateRevision.of(entity.version));
  }

  public static long revision(@NonNull FeedstockBatchJdbcEntity entity) {
    return requireNonNull(entity, "feedstock batch row cannot be null").version;
  }

  public static @NonNull FeedstockBatchSummary toSummary(@NonNull FeedstockBatchJdbcEntity entity) {
    requireNonNull(entity, "feedstock batch row cannot be null");
    return new FeedstockBatchSummary(
        FeedstockBatchId.of(requireNonNull(entity.batchId, "feedstock batch id cannot be null")),
        WorkspaceId.of(requireNonNull(entity.workspaceId, "workspace id cannot be null")),
        BatchName.of(requireNonNull(entity.name, "feedstock batch name cannot be null")),
        entity.vessel == null ? null : Vessel.named(entity.vessel),
        requireNonNull(entity.type, "feedstock type cannot be null"),
        FeedstockSeason.of(
            requireNonNull(entity.seasonStartsOn, "feedstock season start date cannot be null"),
            requireNonNull(entity.seasonEndsOn, "feedstock season end date cannot be null")
        ),
        entity.source == null ? null : FeedstockSource.of(entity.source),
        requireNonNull(entity.lifecycle, "feedstock batch lifecycle cannot be null"),
        VolumeValue.ofLiters(requireNonNull(entity.availableVolumeLiters, "available volume cannot be null")),
        MaterialStateId.of(requireNonNull(
            entity.currentMaterialStateId,
            "feedstock current material state id cannot be null"
        )),
        entity.note
    );
  }

  private static @Nullable String toJdbc(@Nullable Vessel vessel) {
    return vessel == null ? null : vessel.name();
  }
}
