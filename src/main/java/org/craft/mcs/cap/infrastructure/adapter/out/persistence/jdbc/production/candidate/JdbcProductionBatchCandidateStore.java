package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production.candidate;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.candidate.application.model.ProductionBatchCandidateSnapshot;
import org.craft.mcs.cap.core.production.candidate.application.port.out.ProductionBatchCandidateStore;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionBatchCandidateId;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcPersistenceExceptionTranslator.translateOptimisticLocking;


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
    final var entity = repository.findById(snapshot.candidate().candidateId().value())
        .orElseThrow(() -> new ConcurrentResourceModificationException("production batch candidate", null));
    requireExpectedRevision(snapshot, entity);
    final var saved = translateOptimisticLocking(
        "production batch candidate",
        () -> repository.save(ProductionBatchCandidateJdbcMapper.updateFromSnapshot(snapshot, entity, codec))
    );
    return ProductionBatchCandidateJdbcMapper.toSnapshot(saved, codec);
  }

  @Override
  public @NonNull Optional<@NonNull ProductionBatchCandidateSnapshot> findById(
      @NonNull ProductionBatchCandidateId candidateId
  ) {
    requireNonNull(candidateId, "production batch candidate id cannot be null");
    return repository.findById(candidateId.value())
        .map(entity -> ProductionBatchCandidateJdbcMapper.toSnapshot(entity, codec));
  }

  private static void requireExpectedRevision(
      @NonNull ProductionBatchCandidateSnapshot snapshot,
      @NonNull ProductionBatchCandidateJdbcEntity entity
  ) {
    if (entity.version != snapshot.revision()) {
      throw new ConcurrentResourceModificationException("production batch candidate", null);
    }
  }
}
