package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.core.production.domain.model.value.AlcoholicFermentationState;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchLifecycle;
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
  @NonNull @Column("workspace_id") UUID workspaceId;
  @NonNull @Column("name") String name;
  @NonNull @Column("vessel") String vessel;
  @NonNull @Column("lifecycle") ProductionBatchLifecycle lifecycle;
  @NonNull @Column("alcoholic_fermentation_state") AlcoholicFermentationState alcoholicFermentationState;
  @NonNull @Column("current_volume_liters") BigDecimal currentVolumeLiters;
  @NonNull @Column("initial_sugar_specific_gravity") BigDecimal initialSugarSpecificGravity;
  @NonNull @Column("initial_acidity_g_l_as_malic") BigDecimal initialAcidityGLAsMalic;
  @Nullable @Column("initial_tannin_mg_l_tae") BigDecimal initialTanninMgLTae;
  @Nullable @Column("initial_yan_mg_n_l") BigDecimal initialYeastAssimilableNitrogenMgNL;
  @Nullable @Column("note") String note;
  @CreatedDate @Nullable @Column("created_at") Instant createdAt;
  @LastModifiedDate @Nullable @Column("updated_at") Instant updatedAt;
  @Version @Column("version") long version;

  @SuppressWarnings("NullAway.Init")
  public ProductionBatchJdbcEntity() {
  }

  @SuppressWarnings("java:S107")
  ProductionBatchJdbcEntity(
      @NonNull UUID batchId,
      @NonNull UUID workspaceId,
      @NonNull String name,
      @NonNull String vessel,
      @NonNull ProductionBatchLifecycle lifecycle,
      @NonNull AlcoholicFermentationState alcoholicFermentationState,
      @NonNull BigDecimal currentVolumeLiters,
      @NonNull BigDecimal initialSugarSpecificGravity,
      @NonNull BigDecimal initialAcidityGLAsMalic,
      @Nullable BigDecimal initialTanninMgLTae,
      @Nullable BigDecimal initialYeastAssimilableNitrogenMgNL,
      @Nullable String note
  ) {
    this.batchId = requireNonNull(batchId, "production batch id cannot be null");
    this.workspaceId = requireNonNull(workspaceId, "workspace id cannot be null");
    this.name = requireNonNull(name, "production batch name cannot be null");
    this.vessel = requireNonNull(vessel, "production vessel cannot be null");
    this.lifecycle = requireNonNull(lifecycle, "production batch lifecycle cannot be null");
    this.alcoholicFermentationState = requireNonNull(
        alcoholicFermentationState,
        "alcoholic fermentation state cannot be null"
    );
    this.currentVolumeLiters = requireNonNull(currentVolumeLiters, "production current volume cannot be null");
    this.initialSugarSpecificGravity = requireNonNull(
        initialSugarSpecificGravity,
        "production initial sugar specific gravity cannot be null"
    );
    this.initialAcidityGLAsMalic = requireNonNull(
        initialAcidityGLAsMalic,
        "production initial acidity cannot be null"
    );
    this.initialTanninMgLTae = initialTanninMgLTae;
    this.initialYeastAssimilableNitrogenMgNL = initialYeastAssimilableNitrogenMgNL;
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
