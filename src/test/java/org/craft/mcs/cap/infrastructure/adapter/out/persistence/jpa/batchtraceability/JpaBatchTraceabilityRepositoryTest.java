package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.batchtraceability;

import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceId;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceOperationType;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.batchtraceability.entity.BatchTraceStepJpaMapper;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.batchtraceability.entity.BatchTraceStepSubjectRole;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class JpaBatchTraceabilityRepositoryTest {

  @Test
  void appendsStepThroughSpringDataRepository() {
    final var springDataRepository = mock(SpringDataBatchTraceStepJpaRepository.class);
    final var repository = new JpaBatchTraceabilityRepository(springDataRepository);
    final var step = splitStep();

    when(springDataRepository.save(org.mockito.ArgumentMatchers.any()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    final var saved = repository.append(step);

    assertThat(saved).isEqualTo(step);
  }

  @Test
  void findsStepsByInputSubject() {
    final var springDataRepository = mock(SpringDataBatchTraceStepJpaRepository.class);
    final var repository = new JpaBatchTraceabilityRepository(springDataRepository);
    final var subject = BatchTraceSubject.of(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-1");
    final var entity = BatchTraceStepJpaMapper.fromDomain(splitStep());

    when(springDataRepository.findBySubject(
        BatchTraceStepSubjectRole.INPUT,
        subject.type(),
        subject.id()
    ))
        .thenReturn(List.of(entity));

    final var steps = repository.findByInputSubject(subject);

    assertThat(steps).hasSize(1);
    verify(springDataRepository).findBySubject(
        BatchTraceStepSubjectRole.INPUT,
        subject.type(),
        subject.id()
    );
  }

  private static BatchTraceStep splitStep() {
    return BatchTraceStep.create(
        BatchTraceId.newId(),
        null,
        BatchTraceOperationType.SPLIT,
        List.of(BatchTraceStepSubject.of(BatchTraceSubject.of(
            BatchTraceSubjectType.FEEDSTOCK_BATCH,
            "feedstock-1"
        ))),
        List.of(
            BatchTraceStepSubject.of(BatchTraceSubject.of(
                BatchTraceSubjectType.FEEDSTOCK_BATCH,
                "feedstock-1-a"
            )),
            BatchTraceStepSubject.of(BatchTraceSubject.of(
                BatchTraceSubjectType.FEEDSTOCK_BATCH,
                "feedstock-1-b"
            ))
        ),
        Instant.parse("2026-05-18T00:00:00Z"),
        null,
        null
    );
  }
}
