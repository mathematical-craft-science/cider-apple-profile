package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production.candidate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionBatchCandidateStatus;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.AbvPredictionMethod;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.FinalGravityPredictionSource;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PredictionConfidence;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import static java.util.Objects.requireNonNull;


/** Spring Data JDBC aggregate root for a saved production candidate. */
@Table("production_batch_candidates")
class ProductionBatchCandidateJdbcEntity {

  @Id @NonNull @Column("candidate_id") UUID candidateId;
  @NonNull @Column("workspace_id") UUID workspaceId;
  @NonNull @Column("status") ProductionBatchCandidateStatus status;
  @NonNull @Column("sources_json") String sourcesJson;
  @NonNull @Column("total_volume_liters") BigDecimal totalVolumeLiters;
  @NonNull @Column("sugar_specific_gravity") BigDecimal specificGravity;
  @NonNull @Column("acidity_g_l_as_malic") BigDecimal titratableAcidityGLAsMalic;
  @Nullable @Column("tannin_mg_l_tae") BigDecimal tanninMgLTae;
  @Nullable @Column("yan_mg_n_l") BigDecimal yeastAssimilableNitrogenMgNL;
  @NonNull @Column("predicted_final_gravity") BigDecimal predictedFinalGravity;
  @NonNull @Column("predicted_final_gravity_source") FinalGravityPredictionSource predictedFinalGravitySource;
  @NonNull @Column("potential_abv_percent") BigDecimal potentialAbvPercent;
  @NonNull @Column("abv_prediction_method") AbvPredictionMethod abvPredictionMethod;
  @NonNull @Column("abv_prediction_confidence") PredictionConfidence abvPredictionConfidence;
  @Nullable @Column("normalizer_code") ProfileAxisNormalizerCode normalizerCode;
  @Nullable @Column("target_distance_json") String targetDistanceJson;
  @NonNull @Column("warnings_json") String warningsJson;
  @Nullable @Column("confirmed_production_batch_id") UUID confirmedProductionBatchId;
  @CreatedDate @Nullable @Column("created_at") Instant createdAt;
  @LastModifiedDate @Nullable @Column("updated_at") Instant updatedAt;
  @Version @Column("version") long version;

  @SuppressWarnings("NullAway.Init")
  ProductionBatchCandidateJdbcEntity() {
  }

  @SuppressWarnings("java:S107")
  ProductionBatchCandidateJdbcEntity(
      @NonNull UUID candidateId,
      @NonNull UUID workspaceId,
      @NonNull ProductionBatchCandidateStatus status,
      @NonNull String sourcesJson,
      @NonNull BigDecimal totalVolumeLiters,
      @NonNull BigDecimal specificGravity,
      @NonNull BigDecimal titratableAcidityGLAsMalic,
      @Nullable BigDecimal tanninMgLTae,
      @Nullable BigDecimal yeastAssimilableNitrogenMgNL,
      @NonNull BigDecimal predictedFinalGravity,
      @NonNull FinalGravityPredictionSource predictedFinalGravitySource,
      @NonNull BigDecimal potentialAbvPercent,
      @NonNull AbvPredictionMethod abvPredictionMethod,
      @NonNull PredictionConfidence abvPredictionConfidence,
      @Nullable ProfileAxisNormalizerCode normalizerCode,
      @Nullable String targetDistanceJson,
      @NonNull String warningsJson,
      @Nullable UUID confirmedProductionBatchId,
      long version
  ) {
    this.candidateId = requireNonNull(candidateId, "production batch candidate id cannot be null");
    this.workspaceId = requireNonNull(workspaceId, "workspace id cannot be null");
    this.status = requireNonNull(status, "production batch candidate status cannot be null");
    this.sourcesJson = requireNonNull(sourcesJson, "production candidate sources JSON cannot be null");
    this.totalVolumeLiters = requireNonNull(totalVolumeLiters, "production candidate total volume cannot be null");
    this.specificGravity = requireNonNull(
        specificGravity,
        "production candidate specific gravity cannot be null"
    );
    this.titratableAcidityGLAsMalic = requireNonNull(
        titratableAcidityGLAsMalic,
        "production candidate titratable acidity cannot be null"
    );
    this.tanninMgLTae = tanninMgLTae;
    this.yeastAssimilableNitrogenMgNL = yeastAssimilableNitrogenMgNL;
    this.predictedFinalGravity = requireNonNull(
        predictedFinalGravity,
        "production candidate predicted final gravity cannot be null"
    );
    this.predictedFinalGravitySource = requireNonNull(
        predictedFinalGravitySource,
        "production candidate predicted final gravity source cannot be null"
    );
    this.potentialAbvPercent = requireNonNull(
        potentialAbvPercent,
        "production candidate potential ABV cannot be null"
    );
    this.abvPredictionMethod = requireNonNull(
        abvPredictionMethod,
        "production candidate ABV prediction method cannot be null"
    );
    this.abvPredictionConfidence = requireNonNull(
        abvPredictionConfidence,
        "production candidate ABV prediction confidence cannot be null"
    );
    this.normalizerCode = normalizerCode;
    this.targetDistanceJson = targetDistanceJson;
    this.warningsJson = requireNonNull(warningsJson, "production candidate warnings JSON cannot be null");
    this.confirmedProductionBatchId = confirmedProductionBatchId;
    this.version = version;
  }
}
