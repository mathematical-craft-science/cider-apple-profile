package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production.planning;

import java.math.BigDecimal;
import java.util.UUID;
import org.craft.mcs.cap.core.production.planning.domain.model.ProductionBatchCandidateStatus;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.AbvPredictionMethod;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.FinalGravityPredictionSource;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PredictionConfidence;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;


/** Generated CRUD repository for the production-candidate aggregate table. */
interface SpringDataProductionBatchCandidateJdbcRepository
    extends ListCrudRepository<ProductionBatchCandidateJdbcEntity, @NonNull UUID> {

  @Modifying
  @Query("""
      UPDATE production_batch_candidates
         SET status = :status,
             sources_json = :sourcesJson,
             total_volume_liters = :totalVolumeLiters,
             sugar_specific_gravity = :specificGravity,
             acidity_g_l_as_malic = :titratableAcidity,
             tannin_mg_l_tae = :tannin,
             yan_mg_n_l = :yan,
             predicted_final_gravity = :predictedFinalGravity,
             predicted_final_gravity_source = :predictedFinalGravitySource,
             potential_abv_percent = :potentialAbvPercent,
             abv_prediction_method = :abvPredictionMethod,
             abv_prediction_confidence = :abvPredictionConfidence,
             normalizer_code = :normalizerCode,
             target_distance_json = :targetDistanceJson,
             warnings_json = :warningsJson,
             confirmed_production_batch_id = :confirmedProductionBatchId,
             version = version + 1,
             updated_at = GREATEST(clock_timestamp(), created_at, updated_at)
       WHERE candidate_id = :candidateId
         AND version = :expectedVersion
      """)
  @SuppressWarnings("java:S107")
  int updateByIdAndVersion(
      @Param("candidateId") @NonNull UUID candidateId,
      @Param("status") @NonNull ProductionBatchCandidateStatus status,
      @Param("sourcesJson") @NonNull String sourcesJson,
      @Param("totalVolumeLiters") @NonNull BigDecimal totalVolumeLiters,
      @Param("specificGravity") @NonNull BigDecimal specificGravity,
      @Param("titratableAcidity") @NonNull BigDecimal titratableAcidity,
      @Param("tannin") @Nullable BigDecimal tannin,
      @Param("yan") @Nullable BigDecimal yan,
      @Param("predictedFinalGravity") @NonNull BigDecimal predictedFinalGravity,
      @Param("predictedFinalGravitySource") @NonNull FinalGravityPredictionSource predictedFinalGravitySource,
      @Param("potentialAbvPercent") @NonNull BigDecimal potentialAbvPercent,
      @Param("abvPredictionMethod") @NonNull AbvPredictionMethod abvPredictionMethod,
      @Param("abvPredictionConfidence") @NonNull PredictionConfidence abvPredictionConfidence,
      @Param("normalizerCode") @Nullable ProfileAxisNormalizerCode normalizerCode,
      @Param("targetDistanceJson") @Nullable String targetDistanceJson,
      @Param("warningsJson") @NonNull String warningsJson,
      @Param("confirmedProductionBatchId") @Nullable UUID confirmedProductionBatchId,
      @Param("expectedVersion") long expectedVersion
  );
}
