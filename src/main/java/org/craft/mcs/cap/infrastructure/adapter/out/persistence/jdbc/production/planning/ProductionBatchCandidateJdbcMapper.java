package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production.planning;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.production.planning.application.model.ProductionBatchCandidateSnapshot;
import org.craft.mcs.cap.core.production.planning.domain.model.ProductionBatchCandidate;
import org.craft.mcs.cap.core.production.planning.domain.model.ProductionBatchCandidateId;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.batch.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPrediction;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.AbvValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Maps candidate snapshots to and from their Spring Data JDBC aggregate representation. */
final class ProductionBatchCandidateJdbcMapper {

  private ProductionBatchCandidateJdbcMapper() {
  }

  static @NonNull ProductionBatchCandidateJdbcEntity fromSnapshot(
      @NonNull ProductionBatchCandidateSnapshot snapshot,
      @NonNull ProductionBatchCandidateJsonCodec codec
  ) {
    requireNonNull(snapshot, "production batch candidate snapshot cannot be null");
    requireNonNull(codec, "production batch candidate JSON codec cannot be null");
    final var candidate = snapshot.candidate();
    final var profile = candidate.profile();
    return new ProductionBatchCandidateJdbcEntity(
        candidate.candidateId().value(),
        candidate.workspaceId().value(),
        candidate.status(),
        codec.encodeSources(candidate),
        candidate.sourceComposition().totalVolume().liters(),
        profile.specificGravity().asBigDecimal(),
        profile.titratableAcidity().asBigDecimal(),
        tannin(profile),
        yeastAssimilableNitrogen(profile),
        snapshot.potentialAbv().predictedFinalGravity().asBigDecimal(),
        snapshot.potentialAbv().predictedFinalGravitySource(),
        snapshot.potentialAbv().potentialAbv().percent(),
        snapshot.potentialAbv().method(),
        snapshot.potentialAbv().confidence(),
        snapshot.normalizerCode(),
        codec.encodeTargetDistance(snapshot.targetDistance()),
        codec.encodeWarnings(snapshot.warnings()),
        candidate.confirmedProductionBatchId() == null
            ? null
            : candidate.confirmedProductionBatchId().value(),
        snapshot.revision()
    );
  }

  static @NonNull ProductionBatchCandidateSnapshot toSnapshot(
      @NonNull ProductionBatchCandidateJdbcEntity entity,
      @NonNull ProductionBatchCandidateJsonCodec codec
  ) {
    requireNonNull(entity, "production batch candidate entity cannot be null");
    requireNonNull(codec, "production batch candidate JSON codec cannot be null");
    final var sources = codec.decodeSources(entity.sourcesJson);
    final var candidate = ProductionBatchCandidate.restore(
        ProductionBatchCandidateId.of(entity.candidateId),
        WorkspaceId.of(entity.workspaceId),
        sources.composition(),
        sources.stateEvidence(),
        new ProductionProfileSnapshot(
            SpecificGravityValue.of(entity.specificGravity),
            TitratableAcidityValue.of(entity.titratableAcidityGLAsMalic),
            entity.tanninMgLTae == null ? null : TanninValue.of(entity.tanninMgLTae),
            entity.yeastAssimilableNitrogenMgNL == null
                ? null
                : YeastAssimilableNitrogenValue.of(entity.yeastAssimilableNitrogenMgNL)
        ),
        entity.status,
        entity.confirmedProductionBatchId == null
            ? null
            : ProductionBatchId.of(entity.confirmedProductionBatchId)
    );
    return new ProductionBatchCandidateSnapshot(
        candidate,
        new PotentialAbvPrediction(
            candidate.profile().specificGravity(),
            SpecificGravityValue.of(entity.predictedFinalGravity),
            entity.predictedFinalGravitySource,
            AbvValue.of(entity.potentialAbvPercent),
            entity.abvPredictionMethod,
            entity.abvPredictionConfidence
        ),
        entity.normalizerCode,
        codec.decodeTargetDistance(entity.targetDistanceJson),
        codec.decodeWarnings(entity.warningsJson),
        entity.version
    );
  }

  private static @Nullable BigDecimal tannin(@NonNull ProductionProfileSnapshot profile) {
    return profile.tannin() == null ? null : profile.tannin().asBigDecimal();
  }

  private static @Nullable BigDecimal yeastAssimilableNitrogen(
      @NonNull ProductionProfileSnapshot profile
  ) {
    return profile.yeastAssimilableNitrogen() == null
        ? null
        : profile.yeastAssimilableNitrogen().asBigDecimal();
  }
}
