package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.batchtraceability;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import org.craft.mcs.cap.core.batchtraceability.application.port.in.AppendBatchTraceStepUseCase;
import org.craft.mcs.cap.core.batchtraceability.application.usecase.AppendBatchTraceStepService;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceActorReference;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepType;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepSubject;
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
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.testing.BatchTraceSubjects.subject;


@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(
    {
        PostgresDataJdbcTestConfiguration.class,
        JdbcBatchTraceabilityStore.class,
        AppendBatchTraceStepService.class
    }
)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class AppendBatchTraceStepMandatoryDataJdbcTest {

  @Autowired
  private AppendBatchTraceStepUseCase appendUseCase;

  @Autowired
  private TransactionTemplate transactionTemplate;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  private final WorkspaceId workspaceId = WorkspaceId.newId();

  @BeforeEach
  void createWorkspace() {
    final var now = OffsetDateTime.parse("2026-05-18T00:00:00Z");
    jdbcTemplate.update(
        """
            INSERT INTO workspaces (workspace_id, name, type, status, version, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """,
        workspaceId.value(),
        "Traceability transaction test workspace",
        "CIDERY",
        "ACTIVE",
        0L,
        now,
        now
    );
  }

  @Test
  void rejectsAppendOutsideTransaction() {
    final var step = step();

    assertThatThrownBy(() -> appendUseCase.append(step))
        .isInstanceOf(IllegalTransactionStateException.class);
  }

  @Test
  void allowsAppendInsideExistingTransaction() {
    final var step = step();

    assertThatCode(() -> transactionTemplate.executeWithoutResult(status -> appendUseCase.append(step)))
        .doesNotThrowAnyException();
  }

  private BatchTraceStep step() {
    return BatchTraceStep.create(
        workspaceId,
        null,
        BatchTraceStepType.FEEDSTOCK_BATCH_CREATED,
        List.of(),
        List.of(BatchTraceStepSubject.of(subject(
            BatchTraceSubjectType.FEEDSTOCK_BATCH,
            "feedstock-1"
        ))),
        Instant.parse("2026-05-18T00:00:00Z"),
        BatchTraceActorReference.system("test"),
        null
    );
  }
}
