package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.batchtraceability;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityQueryStore;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityStore;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.batchtraceability.entity.BatchTraceStepJpaMapper;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.batchtraceability.entity.BatchTraceStepSubjectRole;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


@Repository
@RequiredArgsConstructor
class JpaBatchTraceabilityRepository
    implements BatchTraceabilityStore, BatchTraceabilityQueryStore {

  private final @NonNull SpringDataBatchTraceStepJpaRepository repository;

  @Override
  public @NonNull BatchTraceStep append(@NonNull BatchTraceStep step) {
    requireNonNull(step, "batch trace step cannot be null");

    final var entity = BatchTraceStepJpaMapper.fromDomain(step);
    final var saved = repository.save(entity);

    return BatchTraceStepJpaMapper.toDomain(saved);
  }

  @Override
  public @NonNull List<@NonNull BatchTraceStep> findByInputSubject(
      @NonNull BatchTraceSubject subject
  ) {
    requireNonNull(subject, "batch trace subject cannot be null");

    return repository.findBySubject(
            BatchTraceStepSubjectRole.INPUT,
            subject.type(),
            subject.id()
        )
        .stream()
        .map(BatchTraceStepJpaMapper::toDomain)
        .toList();
  }
}
