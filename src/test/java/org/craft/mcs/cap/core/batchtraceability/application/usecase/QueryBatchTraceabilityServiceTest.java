package org.craft.mcs.cap.core.batchtraceability.application.usecase;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityQueryStore;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceId;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceOperationType;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class QueryBatchTraceabilityServiceTest {

  @Test
  void queriesStepsByInputSubjectAndProjectsGraph() {
    final var feedstock = BatchTraceSubject.of(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-1");
    final var step = BatchTraceStep.create(
        BatchTraceId.newId(),
        null,
        BatchTraceOperationType.CREATE_PRODUCTION_BATCH,
        List.of(BatchTraceStepSubject.of(feedstock)),
        List.of(BatchTraceStepSubject.of(BatchTraceSubject.of(
            BatchTraceSubjectType.PRODUCTION_BATCH,
            "production-1"
        ))),
        Instant.parse("2026-05-18T00:00:00Z"),
        null,
        null
    );
    final var queryStore = new StubQueryStore(List.of(step));
    final var service = new QueryBatchTraceabilityService(queryStore);

    final var graph = service.graphFor(feedstock);

    assertThat(queryStore.requestedSubject).isEqualTo(feedstock);
    assertThat(graph.nodes()).hasSize(2);
    assertThat(graph.edges()).hasSize(1);
  }

  @Test
  void queriesStepsByOutputSubjectAndProjectsSourceGraph() {
    final var production = BatchTraceSubject.of(BatchTraceSubjectType.PRODUCTION_BATCH, "production-1");
    final var step = BatchTraceStep.create(
        BatchTraceId.newId(),
        null,
        BatchTraceOperationType.CREATE_PRODUCTION_BATCH,
        List.of(BatchTraceStepSubject.of(BatchTraceSubject.of(
            BatchTraceSubjectType.FEEDSTOCK_BATCH,
            "feedstock-1"
        ))),
        List.of(BatchTraceStepSubject.of(production)),
        Instant.parse("2026-05-18T00:00:00Z"),
        null,
        null
    );
    final var queryStore = new StubQueryStore(List.of(step));
    final var service = new QueryBatchTraceabilityService(queryStore);

    final var graph = service.sourceGraphFor(production);

    assertThat(queryStore.requestedOutputSubjects).contains(production);
    assertThat(graph.nodes()).hasSize(2);
    assertThat(graph.edges()).hasSize(1);
  }

  private static final class StubQueryStore implements BatchTraceabilityQueryStore {

    private final List<BatchTraceStep> steps;
    private BatchTraceSubject requestedSubject;
    private final List<BatchTraceSubject> requestedOutputSubjects = new ArrayList<>();

    private StubQueryStore(List<BatchTraceStep> steps) {
      this.steps = List.copyOf(steps);
    }

    @Override
    public @NonNull List<@NonNull BatchTraceStep> findByInputSubject(
        @NonNull BatchTraceSubject subject
    ) {
      requestedSubject = subject;
      return steps;
    }

    @Override
    public @NonNull List<@NonNull BatchTraceStep> findByOutputSubject(
        @NonNull BatchTraceSubject subject
    ) {
      requestedOutputSubjects.add(subject);
      return steps;
    }
  }
}
