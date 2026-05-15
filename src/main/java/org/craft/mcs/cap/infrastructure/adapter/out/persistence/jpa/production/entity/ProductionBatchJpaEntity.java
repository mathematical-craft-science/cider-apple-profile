package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.production.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchKind;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import static java.util.Objects.requireNonNull;


@Entity
@Table(name = "production_batches")
@EntityListeners(AuditingEntityListener.class)
public class ProductionBatchJpaEntity {

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
  @Column(name = "kind", nullable = false, length = 32)
  ProductionBatchKind kind;

  @NonNull
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 32)
  ProductionBatchStatus status;

  @NonNull
  @Column(name = "total_volume_liters", nullable = false, precision = 15, scale = 3)
  BigDecimal totalVolumeLiters;

  @NonNull
  @Column(name = "sugar_specific_gravity", nullable = false, precision = 12, scale = 9)
  BigDecimal sugarSpecificGravity;

  @NonNull
  @Column(name = "acidity_g_l_as_malic", nullable = false, precision = 12, scale = 6)
  BigDecimal acidityGLAsMalic;

  @Nullable
  @Column(name = "tannin_mg_l_tae", precision = 12, scale = 4)
  BigDecimal tanninMgLTea;

  @Nullable
  @Column(name = "target_distance", precision = 18, scale = 9)
  BigDecimal targetDistance;

  @Nullable
  @Column(name = "note", length = 2000)
  String note;

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

  @OneToMany(mappedBy = "productionBatch", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @NonNull
  List<@NonNull ProductionBatchSourceJpaEntity> sources = new ArrayList<>();

  @SuppressWarnings("NullAway.Init")
  protected ProductionBatchJpaEntity() {
  }

  ProductionBatchJpaEntity(
      @NonNull UUID batchId,
      @NonNull UUID ciderMakerId,
      @NonNull String name,
      @NonNull ProductionBatchKind kind,
      @NonNull ProductionBatchStatus status,
      @NonNull BigDecimal totalVolumeLiters,
      @NonNull BigDecimal sugarSpecificGravity,
      @NonNull BigDecimal acidityGLAsMalic,
      @Nullable BigDecimal tanninMgLTea,
      @Nullable BigDecimal targetDistance,
      @Nullable String note
  ) {
    this.batchId = requireNonNull(batchId, "production batch id cannot be null");
    this.ciderMakerId = requireNonNull(ciderMakerId, "cider maker id cannot be null");
    this.name = requireNonNull(name, "production batch name cannot be null");
    this.kind = requireNonNull(kind, "production batch kind cannot be null");
    this.status = requireNonNull(status, "production batch status cannot be null");
    this.totalVolumeLiters = requireNonNull(totalVolumeLiters, "production total volume cannot be null");
    this.sugarSpecificGravity = requireNonNull(sugarSpecificGravity, "production sugar specific gravity cannot be null");
    this.acidityGLAsMalic = requireNonNull(acidityGLAsMalic, "production acidity cannot be null");
    this.tanninMgLTea = tanninMgLTea;
    this.targetDistance = targetDistance;
    this.note = normalize(note);
  }

  void replaceSources(@NonNull List<@NonNull ProductionBatchSourceJpaEntity> sourceEntities) {
    requireNonNull(sourceEntities, "production batch source entities cannot be null");

    sources.clear();
    for (final var source : sourceEntities) {
      source.productionBatch = this;
      sources.add(source);
    }
  }

  private static @Nullable String normalize(@Nullable String value) {
    if (value == null) {
      return null;
    }

    final var normalized = value.strip();
    return normalized.isEmpty() ? null : normalized;
  }
}
