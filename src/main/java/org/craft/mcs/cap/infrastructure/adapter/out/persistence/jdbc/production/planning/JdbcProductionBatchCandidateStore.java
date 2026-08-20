package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production.planning;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionBatchCandidateSnapshot;
import org.craft.mcs.cap.core.production.planning.application.port.out.ProductionBatchCandidateStore;
import org.craft.mcs.cap.core.production.planning.domain.model.ProductionBatchCandidateId;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.DataRetrievalFailureException;
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
    return toSnapshot(saved);
  }

  @Override
  public @NonNull ProductionBatchCandidateSnapshot update(
      @NonNull ProductionBatchCandidateSnapshot snapshot
  ) {
    requireNonNull(snapshot, "production batch candidate snapshot cannot be null");
    final var candidate = snapshot.candidate();
    final var profile = candidate.profile();
    final var prediction = snapshot.potentialAbv();
    final var tannin = profile.tannin();
    final var yeastAssimilableNitrogen = profile.yeastAssimilableNitrogen();
    final var confirmedProductionBatchId = candidate.confirmedProductionBatchId();
    final var affectedRows = translateOptimisticLocking(
        "production batch candidate",
        () -> repository.updateByIdAndVersion(
            candidate.candidateId().value(),
            candidate.status(),
            codec.encodeSources(candidate),
            candidate.sourceComposition().totalVolume().liters(),
            profile.specificGravity().asBigDecimal(),
            profile.titratableAcidity().asBigDecimal(),
            tannin == null ? null : tannin.asBigDecimal(),
            yeastAssimilableNitrogen == null
                ? null
                : yeastAssimilableNitrogen.asBigDecimal(),
            prediction.predictedFinalGravity().asBigDecimal(),
            prediction.predictedFinalGravitySource(),
            prediction.potentialAbv().percent(),
            prediction.method(),
            prediction.confidence(),
            snapshot.normalizerCode(),
            codec.encodeTargetDistance(snapshot.targetDistance()),
            codec.encodeWarnings(snapshot.warnings()),
            confirmedProductionBatchId == null
                ? null
                : confirmedProductionBatchId.value(),
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
        .map(this::toSnapshot);
  }

  @Override
  public @NonNull Optional<@NonNull ProductionBatchCandidateSnapshot> lockById(
      @NonNull ProductionBatchCandidateId candidateId
  ) {
    requireNonNull(candidateId, "production batch candidate id cannot be null");
    return repository.lockById(candidateId.value())
        .map(this::toSnapshot);
  }

  private @NonNull ProductionBatchCandidateSnapshot toSnapshot(
      @NonNull ProductionBatchCandidateJdbcEntity entity
  ) {
    try {
      return ProductionBatchCandidateJdbcMapper.toSnapshot(entity, codec);
    } catch (DataRetrievalFailureException exception) {
      throw exception;
    } catch (RuntimeException exception) {
      throw new DataRetrievalFailureException("persisted production batch candidate is invalid", exception);
    }
  }

}
