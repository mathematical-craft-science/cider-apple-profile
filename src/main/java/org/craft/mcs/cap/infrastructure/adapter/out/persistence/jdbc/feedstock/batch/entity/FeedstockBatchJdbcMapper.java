package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.batch.entity;

import static java.util.Objects.requireNonNull;

import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSource;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;


public final class FeedstockBatchJdbcMapper {

  private FeedstockBatchJdbcMapper() {
  }

  public static @NonNull FeedstockBatchJdbcEntity fromDomain(@NonNull FeedstockBatch batch) {
    requireNonNull(batch, "feedstock batch cannot be null");

    return new FeedstockBatchJdbcEntity(
        batch.batchId().value(),
        batch.workspaceId().value(),
        batch.name().value(),
        batch.vesselId().value(),
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

  public static @NonNull FeedstockBatch toDomain(@NonNull FeedstockBatchJdbcEntity entity) {
    requireNonNull(entity, "feedstock batch row cannot be null");

    final var source = entity.source == null ? null : FeedstockSource.of(entity.source);

    return FeedstockBatch.restore(
        FeedstockBatchId.of(requireNonNull(entity.batchId, "feedstock batch id cannot be null")),
        WorkspaceId.of(requireNonNull(entity.workspaceId, "workspace id cannot be null")),
        BatchName.of(requireNonNull(entity.name, "feedstock batch name cannot be null")),
        VesselId.of(requireNonNull(entity.vesselId, "feedstock vessel id cannot be null")),
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

  public static @NonNull FeedstockBatchSummary toSummary(@NonNull FeedstockBatchJdbcEntity entity) {
    requireNonNull(entity, "feedstock batch row cannot be null");
    return new FeedstockBatchSummary(
        FeedstockBatchId.of(requireNonNull(entity.batchId, "feedstock batch id cannot be null")),
        WorkspaceId.of(requireNonNull(entity.workspaceId, "workspace id cannot be null")),
        BatchName.of(requireNonNull(entity.name, "feedstock batch name cannot be null")),
        VesselId.of(requireNonNull(entity.vesselId, "feedstock vessel id cannot be null")),
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
}
