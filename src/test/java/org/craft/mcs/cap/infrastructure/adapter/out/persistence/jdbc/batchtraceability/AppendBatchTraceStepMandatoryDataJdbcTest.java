package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.batchtraceability;

import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.core.batchtraceability.application.port.in.AppendBatchTraceStepUseCase;
import org.craft.mcs.cap.core.batchtraceability.application.usecase.AppendBatchTraceStepService;
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
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    PostgresDataJdbcTestConfiguration.class,
    JdbcBatchTraceabilityRepository.class,
    AppendBatchTraceStepService.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class AppendBatchTraceStepMandatoryDataJdbcTest {

  @Autowired
  private AppendBatchTraceStepUseCase appendUseCase;

  @Autowired
  private TransactionTemplate transactionTemplate;

  @Test
  void rejectsAppendOutsideTransaction() {
    assertThatThrownBy(() -> appendUseCase.append(step()))
        .isInstanceOf(IllegalTransactionStateException.class);
  }

  @Test
  void allowsAppendInsideExistingTransaction() {
    assertThatCode(() -> transactionTemplate.executeWithoutResult(status -> appendUseCase.append(step())))
        .doesNotThrowAnyException();
  }

  private static BatchTraceStep step() {
    return BatchTraceStep.create(
        BatchTraceId.newId(),
        null,
        BatchTraceOperationType.CREATE_FEEDSTOCK_BATCH,
        List.of(),
        List.of(BatchTraceStepSubject.of(BatchTraceSubject.of(
            BatchTraceSubjectType.FEEDSTOCK_BATCH,
            "feedstock-1"
        ))),
        Instant.parse("2026-05-18T00:00:00Z"),
        null,
        null
    );
  }
}
