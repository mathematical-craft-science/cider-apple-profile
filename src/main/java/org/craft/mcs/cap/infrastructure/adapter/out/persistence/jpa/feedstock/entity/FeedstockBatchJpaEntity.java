package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.feedstock.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import static java.util.Objects.requireNonNull;


@Entity
@Table(name = "feedstock_batches")
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = """
    UPDATE feedstock_batches
    SET deleted_at = CURRENT_TIMESTAMP,
        updated_at = CURRENT_TIMESTAMP,
        version = version + 1
    WHERE batch_id = ? AND version = ?
    """)
@SQLRestriction("deleted_at IS NULL")
public class FeedstockBatchJpaEntity {

  @Id
  @NonNull
  @Column(name = "batch_id", nullable = false, updatable = false)
  UUID batchId;

  @NonNull
  @Column(name = "cider_maker_id", nullable = false, updatable = false)
  UUID ciderMakerId;

  @NonNull
  @Column(name = "name", nullable = false, length = 160)
  String name;

  @NonNull
  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 32)
  FeedstockType type;

  @NonNull
  @Column(name = "season_starts_on", nullable = false)
  LocalDate seasonStartsOn;

  @NonNull
  @Column(name = "season_ends_on", nullable = false)
  LocalDate seasonEndsOn;

  @Nullable
  @Column(name = "source", length = 255)
  String source;

  @NonNull
  @Column(name = "available_volume_liters", nullable = false, precision = 15, scale = 3)
  BigDecimal availableVolumeLiters;

  @Nullable
  @Column(name = "note", length = 2000)
  String note;

  @Nullable
  @Column(name = "deleted_at")
  Instant deletedAt;

  @NonNull
  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  Instant createdAt;

  @NonNull
  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  Instant updatedAt;

  @Version
  @Column(name = "version", nullable = false)
  long version;

  @SuppressWarnings("NullAway.Init")
  protected FeedstockBatchJpaEntity() {
  }

  FeedstockBatchJpaEntity(
      @NonNull UUID batchId,
      @NonNull UUID ciderMakerId,
      @NonNull String name,
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
    this.type = requireNonNull(type, "feedstock type cannot be null");
    this.seasonStartsOn = requireNonNull(seasonStartsOn, "feedstock season start date cannot be null");
    this.seasonEndsOn = requireNonNull(seasonEndsOn, "feedstock season end date cannot be null");
    this.source = normalize(source);
    this.availableVolumeLiters = requireNonNull(availableVolumeLiters, "available volume cannot be null");
    this.note = normalize(note);
    this.deletedAt = deletedAt;
  }

  @NonNull
  UUID id() {
    return requireNonNull(batchId, "feedstock batch id cannot be null");
  }

  private static @Nullable String normalize(@Nullable String value) {
    if (value == null) {
      return null;
    }

    final var normalized = value.strip();
    return normalized.isEmpty() ? null : normalized;
  }
}
