package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchKind;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import static java.util.Objects.requireNonNull;


@Table("production_batches")
public class ProductionBatchJdbcEntity {

  @Id @NonNull @Column("batch_id") UUID batchId;
  @NonNull @Column("cider_maker_id") UUID ciderMakerId;
  @NonNull @Column("name") String name;
  @NonNull @Column("kind") ProductionBatchKind kind;
  @NonNull @Column("status") ProductionBatchStatus status;
  @NonNull @Column("total_volume_liters") BigDecimal totalVolumeLiters;
  @NonNull @Column("sugar_specific_gravity") BigDecimal sugarSpecificGravity;
  @NonNull @Column("acidity_g_l_as_malic") BigDecimal acidityGLAsMalic;
  @Nullable @Column("tannin_mg_l_tae") BigDecimal tanninMgLTea;
  @Nullable @Column("yan_mg_n_l") BigDecimal yeastAssimilableNitrogenMgNL;
  @Nullable @Column("target_distance") BigDecimal targetDistance;
  @Nullable @Column("note") String note;
  @CreatedDate @Nullable @Column("created_at") Instant createdAt;
  @LastModifiedDate @Nullable @Column("updated_at") Instant updatedAt;
  @Version @Column("version") long version;

  @SuppressWarnings("NullAway.Init")
  public ProductionBatchJdbcEntity() {
  }

  ProductionBatchJdbcEntity(
      @NonNull UUID batchId,
      @NonNull UUID ciderMakerId,
      @NonNull String name,
      @NonNull ProductionBatchKind kind,
      @NonNull ProductionBatchStatus status,
      @NonNull BigDecimal totalVolumeLiters,
      @NonNull BigDecimal sugarSpecificGravity,
      @NonNull BigDecimal acidityGLAsMalic,
      @Nullable BigDecimal tanninMgLTea,
      @Nullable BigDecimal yeastAssimilableNitrogenMgNL,
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
    this.yeastAssimilableNitrogenMgNL = yeastAssimilableNitrogenMgNL;
    this.targetDistance = targetDistance;
    this.note = normalize(note);
  }

  private static @Nullable String normalize(@Nullable String value) {
    if (value == null) {
      return null;
    }

    final var normalized = value.strip();
    return normalized.isEmpty() ? null : normalized;
  }
}
