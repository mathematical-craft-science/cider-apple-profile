package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production.candidate;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.production.candidate.application.model.ProductionBatchCandidateSnapshot;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionBatchCandidate;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionBatchCandidateId;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPrediction;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.AbvValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
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
        profile.sugar().asBigDecimal(),
        profile.acidity().asBigDecimal(),
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

  static @NonNull ProductionBatchCandidateJdbcEntity updateFromSnapshot(
      @NonNull ProductionBatchCandidateSnapshot snapshot,
      @NonNull ProductionBatchCandidateJdbcEntity entity,
      @NonNull ProductionBatchCandidateJsonCodec codec
  ) {
    requireNonNull(snapshot, "production batch candidate snapshot cannot be null");
    requireNonNull(entity, "production batch candidate entity cannot be null");
    requireNonNull(codec, "production batch candidate JSON codec cannot be null");
    final var candidate = snapshot.candidate();
    final var profile = candidate.profile();
    entity.workspaceId = candidate.workspaceId().value();
    entity.status = candidate.status();
    entity.sourcesJson = codec.encodeSources(candidate);
    entity.totalVolumeLiters = candidate.sourceComposition().totalVolume().liters();
    entity.sugarSpecificGravity = profile.sugar().asBigDecimal();
    entity.acidityGLAsMalic = profile.acidity().asBigDecimal();
    entity.tanninMgLTae = tannin(profile);
    entity.yeastAssimilableNitrogenMgNL = yeastAssimilableNitrogen(profile);
    entity.predictedFinalGravity = snapshot.potentialAbv().predictedFinalGravity().asBigDecimal();
    entity.predictedFinalGravitySource = snapshot.potentialAbv().predictedFinalGravitySource();
    entity.potentialAbvPercent = snapshot.potentialAbv().potentialAbv().percent();
    entity.abvPredictionMethod = snapshot.potentialAbv().method();
    entity.abvPredictionConfidence = snapshot.potentialAbv().confidence();
    entity.normalizerCode = snapshot.normalizerCode();
    entity.targetDistanceJson = codec.encodeTargetDistance(snapshot.targetDistance());
    entity.warningsJson = codec.encodeWarnings(snapshot.warnings());
    entity.confirmedProductionBatchId = candidate.confirmedProductionBatchId() == null
        ? null
        : candidate.confirmedProductionBatchId().value();
    return entity;
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
        sources.evidence(),
        new ProductionProfileSnapshot(
            SpecificGravityValue.of(entity.sugarSpecificGravity),
            TitratableAcidityValue.of(entity.acidityGLAsMalic),
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
            candidate.profile().sugar(),
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
