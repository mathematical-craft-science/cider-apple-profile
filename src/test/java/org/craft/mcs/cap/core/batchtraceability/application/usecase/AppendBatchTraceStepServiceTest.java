package org.craft.mcs.cap.core.batchtraceability.application.usecase;

import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityStore;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceId;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceOperationType;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class AppendBatchTraceStepServiceTest {

  @Test
  void appendsStepThroughStore() {
    final var store = new CapturingStore();
    final var service = new AppendBatchTraceStepService(store);
    final var step = BatchTraceStep.create(
        BatchTraceId.newId(),
        null,
        BatchTraceOperationType.CREATE_PRODUCTION_BATCH,
        List.of(),
        List.of(BatchTraceStepSubject.of(BatchTraceSubject.of(
            BatchTraceSubjectType.PRODUCTION_BATCH,
            "production-1"
        ))),
        Instant.parse("2026-05-18T00:00:00Z"),
        null,
        null
    );

    final var stepId = service.append(step);

    assertThat(stepId).isEqualTo(step.stepId());
    assertThat(store.saved).isEqualTo(step);
  }

  private static final class CapturingStore implements BatchTraceabilityStore {

    private BatchTraceStep saved;

    @Override
    public @NonNull BatchTraceStep append(@NonNull BatchTraceStep step) {
      saved = step;
      return step;
    }
  }
}
