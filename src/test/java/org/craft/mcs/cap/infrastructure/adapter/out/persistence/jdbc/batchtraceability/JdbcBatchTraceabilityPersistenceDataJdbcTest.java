package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.batchtraceability;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityQueryStore;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityStore;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceActorReference;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceOperationType;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;


@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    PostgresDataJdbcTestConfiguration.class,
    JdbcBatchTraceabilityStore.class,
    JdbcBatchTraceabilityQueryStore.class
})
class JdbcBatchTraceabilityPersistenceDataJdbcTest {

  @Autowired
  private BatchTraceabilityStore store;

  @Autowired
  private BatchTraceabilityQueryStore queryStore;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  private final WorkspaceId workspaceId = WorkspaceId.newId();

  @BeforeEach
  void createWorkspace() {
    insertWorkspace(workspaceId, "Traceability test workspace");
  }

  private void insertWorkspace(
      WorkspaceId id,
      String name
  ) {
    final var now = OffsetDateTime.parse("2026-05-18T00:00:00Z");
    jdbcTemplate.update(
        """
            INSERT INTO workspaces (workspace_id, name, type, status, version, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """,
        id.value(),
        name,
        "CIDERY",
        "ACTIVE",
        0L,
        now,
        now
    );
  }

  @Test
  void isolatesSubjectQueriesByWorkspace() {
    final var otherWorkspaceId = WorkspaceId.newId();
    insertWorkspace(otherWorkspaceId, "Other traceability test workspace");
    final var sharedSubject = BatchTraceSubject.of(BatchTraceSubjectType.FEEDSTOCK_BATCH, "shared-subject");
    final var firstStep = splitStep(workspaceId, sharedSubject, "first-output");
    final var otherStep = splitStep(otherWorkspaceId, sharedSubject, "other-output");

    store.append(firstStep);
    store.append(otherStep);

    assertThat(queryStore.findByInputSubject(workspaceId, sharedSubject))
        .singleElement()
        .returns(workspaceId, BatchTraceStep::workspaceId)
        .returns(firstStep.stepId(), BatchTraceStep::stepId);
  }

  @Test
  void persistsAndFindsStepByInputSubjectWithSeveralOutputs() {
    final var input = BatchTraceSubject.of(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-1");
    final var step = BatchTraceStep.create(
        workspaceId,
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
        BatchTraceActorReference.system("test"),
        null
    );

    store.append(step);

    final var found = queryStore.findByInputSubject(workspaceId, input);

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
        workspaceId,
        null,
        BatchTraceOperationType.CREATE_PRODUCTION_BATCH,
        List.of(BatchTraceStepSubject.of(BatchTraceSubject.of(
            BatchTraceSubjectType.FEEDSTOCK_BATCH,
            "feedstock-1"
        ))),
        List.of(BatchTraceStepSubject.of(output)),
        Instant.parse("2026-05-18T00:00:00Z"),
        BatchTraceActorReference.system("test"),
        null
    );

    store.append(step);

    final var found = queryStore.findByOutputSubject(workspaceId, output);

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

  private static BatchTraceStep splitStep(
      WorkspaceId workspaceId,
      BatchTraceSubject input,
      String outputId
  ) {
    return BatchTraceStep.create(
        workspaceId,
        null,
        BatchTraceOperationType.SPLIT,
        List.of(BatchTraceStepSubject.of(input)),
        List.of(BatchTraceStepSubject.of(BatchTraceSubject.of(
            BatchTraceSubjectType.FEEDSTOCK_BATCH,
            outputId
        ))),
        Instant.parse("2026-05-18T00:00:00Z"),
        BatchTraceActorReference.system("test"),
        null
    );
  }
}
