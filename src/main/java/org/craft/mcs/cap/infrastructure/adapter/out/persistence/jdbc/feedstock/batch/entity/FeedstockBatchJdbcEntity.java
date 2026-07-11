package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.batch.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import static java.util.Objects.requireNonNull;


@Table("feedstock_batches")
public class FeedstockBatchJdbcEntity {

  @Id @NonNull @Column("batch_id") UUID batchId;
  @NonNull @Column("cider_maker_id") UUID ciderMakerId;
  @NonNull @Column("name") String name;
  @Nullable @Column("vessel") String vessel;
  @NonNull @Column("type") FeedstockType type;
  @NonNull @Column("season_starts_on") LocalDate seasonStartsOn;
  @NonNull @Column("season_ends_on") LocalDate seasonEndsOn;
  @Nullable @Column("source") String source;
  @NonNull @Column("available_volume_liters") BigDecimal availableVolumeLiters;
  @Nullable @Column("note") String note;
  @Nullable @Column("deleted_at") Instant deletedAt;
  @CreatedDate @Nullable @Column("created_at") Instant createdAt;
  @LastModifiedDate @Nullable @Column("updated_at") Instant updatedAt;
  @Version @Column("version") long version;

  @SuppressWarnings("NullAway.Init")
  public FeedstockBatchJdbcEntity() {
  }

  FeedstockBatchJdbcEntity(
      @NonNull UUID batchId,
      @NonNull UUID ciderMakerId,
      @NonNull String name,
      @Nullable String vessel,
      @NonNull FeedstockType type,
      @NonNull LocalDate seasonStartsOn,
      @NonNull LocalDate seasonEndsOn,
      @Nullable String source,
      @NonNull BigDecimal availableVolumeLiters,
      @Nullable String note,
      @Nullable Instant deletedAt
  ) {
    this.batchId = requireNonNull(batchId, "feedstock batch id cannot be null");
    this.ciderMakerId = requireNonNull(ciderMakerId, "cider maker id cannot be null");
    this.name = requireNonNull(name, "feedstock batch name cannot be null");
    this.vessel = normalize(vessel);
    this.type = requireNonNull(type, "feedstock type cannot be null");
    this.seasonStartsOn = requireNonNull(seasonStartsOn, "feedstock season start date cannot be null");
    this.seasonEndsOn = requireNonNull(seasonEndsOn, "feedstock season end date cannot be null");
    this.source = normalize(source);
    this.availableVolumeLiters = requireNonNull(availableVolumeLiters, "available volume cannot be null");
    this.note = normalize(note);
    this.deletedAt = deletedAt;
  }

  private static @Nullable String normalize(@Nullable String value) {
    if (value == null) {
      return null;
    }

    final var normalized = value.strip();
    return normalized.isEmpty() ? null : normalized;
  }
}
