package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production.candidate;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.candidate.application.model.ProductionBatchCandidateSnapshot;
import org.craft.mcs.cap.core.production.candidate.application.port.out.ProductionBatchCandidateStore;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionBatchCandidateId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcPersistenceExceptionTranslator.translateOptimisticLocking;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcVersionedUpdate.requireSingleRow;


/** Persists candidate snapshots while preserving optimistic-locking semantics. */
@Repository
@RequiredArgsConstructor
class JdbcProductionBatchCandidateStore implements ProductionBatchCandidateStore {

  private final @NonNull SpringDataProductionBatchCandidateJdbcRepository repository;
  private final @NonNull ProductionBatchCandidateJsonCodec codec;

  @Override
  public @NonNull ProductionBatchCandidateSnapshot create(
      @NonNull ProductionBatchCandidateSnapshot snapshot
  ) {
    requireNonNull(snapshot, "production batch candidate snapshot cannot be null");
    final var saved = translateOptimisticLocking(
        "production batch candidate",
        () -> repository.save(ProductionBatchCandidateJdbcMapper.fromSnapshot(snapshot, codec))
    );
    return ProductionBatchCandidateJdbcMapper.toSnapshot(saved, codec);
  }

  @Override
  public @NonNull ProductionBatchCandidateSnapshot update(
      @NonNull ProductionBatchCandidateSnapshot snapshot
  ) {
    requireNonNull(snapshot, "production batch candidate snapshot cannot be null");
    final var candidate = snapshot.candidate();
    final var profile = candidate.profile();
    final var prediction = snapshot.potentialAbv();
    final var affectedRows = translateOptimisticLocking(
        "production batch candidate",
        () -> repository.updateByIdAndVersion(
            candidate.candidateId().value(),
            candidate.status(),
            codec.encodeSources(candidate),
            candidate.sourceComposition().totalVolume().liters(),
            profile.specificGravity().asBigDecimal(),
            profile.titratableAcidity().asBigDecimal(),
            profile.tannin() == null ? null : profile.tannin().asBigDecimal(),
            profile.yeastAssimilableNitrogen() == null
                ? null
                : profile.yeastAssimilableNitrogen().asBigDecimal(),
            prediction.predictedFinalGravity().asBigDecimal(),
            prediction.predictedFinalGravitySource(),
            prediction.potentialAbv().percent(),
            prediction.method(),
            prediction.confidence(),
            snapshot.normalizerCode(),
            codec.encodeTargetDistance(snapshot.targetDistance()),
            codec.encodeWarnings(snapshot.warnings()),
            candidate.confirmedProductionBatchId() == null
                ? null
                : candidate.confirmedProductionBatchId().value(),
            snapshot.revision()
        )
    );
    requireSingleRow("production batch candidate", affectedRows);
    return snapshot.withRevision(snapshot.revision() + 1);
  }

  @Override
  public @NonNull Optional<@NonNull ProductionBatchCandidateSnapshot> findById(
      @NonNull ProductionBatchCandidateId candidateId
  ) {
    requireNonNull(candidateId, "production batch candidate id cannot be null");
    return repository.findById(candidateId.value())
        .map(entity -> ProductionBatchCandidateJdbcMapper.toSnapshot(entity, codec));
  }

}
