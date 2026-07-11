package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.batch.entity;

import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockBatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSource;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockVessel;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
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
        batch.ciderMakerId().value(),
        batch.name().value(),
        toJdbc(batch.vessel()),
        batch.type(),
        batch.season().startsOn(),
        batch.season().endsOn(),
        batch.source() == null ? null : batch.source().value(),
        batch.availableVolume().liters(),
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
    entity.availableVolumeLiters = batch.availableVolume().liters();
    entity.note = batch.note();
    entity.deletedAt = batch.deletedAt();

    return entity;
  }

  public static @NonNull FeedstockBatch toDomain(@NonNull FeedstockBatchJdbcEntity entity) {
    requireNonNull(entity, "feedstock batch row cannot be null");

    final var source = entity.source == null ? null : FeedstockSource.of(entity.source);

    return FeedstockBatch.restore(
        FeedstockBatchId.of(requireNonNull(entity.batchId, "feedstock batch id cannot be null")),
        CiderMakerId.of(requireNonNull(entity.ciderMakerId, "cider maker id cannot be null")),
        FeedstockBatchName.of(requireNonNull(entity.name, "feedstock batch name cannot be null")),
        entity.vessel == null ? null : FeedstockVessel.of(entity.vessel),
        requireNonNull(entity.type, "feedstock type cannot be null"),
        FeedstockSeason.of(
            requireNonNull(entity.seasonStartsOn, "feedstock season start date cannot be null"),
            requireNonNull(entity.seasonEndsOn, "feedstock season end date cannot be null")
        ),
        source,
        VolumeValue.ofLiters(requireNonNull(entity.availableVolumeLiters, "available volume cannot be null")),
        entity.note,
        entity.deletedAt
    );
  }

  private static @Nullable String toJdbc(@Nullable FeedstockVessel vessel) {
    return vessel == null ? null : vessel.value();
  }
}
