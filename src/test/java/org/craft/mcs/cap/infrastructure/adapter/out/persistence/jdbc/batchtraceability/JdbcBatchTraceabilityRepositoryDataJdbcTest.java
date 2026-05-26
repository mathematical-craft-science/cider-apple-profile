package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.batchtraceability;

import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityQueryStore;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityStore;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceId;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceOperationType;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;


@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    PostgresDataJdbcTestConfiguration.class,
    JdbcBatchTraceabilityRepository.class
})
class JdbcBatchTraceabilityRepositoryDataJdbcTest {

  @Autowired
  private BatchTraceabilityStore store;

  @Autowired
  private BatchTraceabilityQueryStore queryStore;

  @Test
  void persistsAndFindsStepByInputSubjectWithSeveralOutputs() {
    final var input = BatchTraceSubject.of(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-1");
    final var step = BatchTraceStep.create(
        BatchTraceId.newId(),
        null,
        BatchTraceOperationType.SPLIT,
        List.of(BatchTraceStepSubject.of(input)),
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

    store.append(step);

    final var found = queryStore.findByInputSubject(input);

    assertThat(found).singleElement()
        .satisfies(foundStep -> {
          assertThat(foundStep.stepId()).isEqualTo(step.stepId());
          assertThat(foundStep.operationType()).isEqualTo(BatchTraceOperationType.SPLIT);
          assertThat(foundStep.occurredAt()).isEqualTo(Instant.parse("2026-05-18T00:00:00Z"));
          assertThat(foundStep.inputs()).hasSize(1);
          assertThat(foundStep.outputs())
              .extracting(output -> output.subject().id())
              .containsExactly("feedstock-1-a", "feedstock-1-b");
        });
  }

  @Test
  void persistsAndFindsStepByOutputSubject() {
    final var output = BatchTraceSubject.of(BatchTraceSubjectType.PRODUCTION_BATCH, "production-1");
    final var step = BatchTraceStep.create(
        BatchTraceId.newId(),
        null,
        BatchTraceOperationType.CREATE_PRODUCTION_BATCH,
        List.of(BatchTraceStepSubject.of(BatchTraceSubject.of(
            BatchTraceSubjectType.FEEDSTOCK_BATCH,
            "feedstock-1"
        ))),
        List.of(BatchTraceStepSubject.of(output)),
        Instant.parse("2026-05-18T00:00:00Z"),
        null,
        null
    );

    store.append(step);

    final var found = queryStore.findByOutputSubject(output);

    assertThat(found).singleElement()
        .satisfies(foundStep -> {
          assertThat(foundStep.stepId()).isEqualTo(step.stepId());
          assertThat(foundStep.occurredAt()).isEqualTo(Instant.parse("2026-05-18T00:00:00Z"));
          assertThat(foundStep.inputs())
              .extracting(input -> input.subject().id())
              .containsExactly("feedstock-1");
          assertThat(foundStep.outputs())
              .extracting(foundOutput -> foundOutput.subject().id())
              .containsExactly("production-1");
        });
  }
}
