package org.craft.mcs.cap.core.traceability.application.usecase;

import org.craft.mcs.cap.core.traceability.application.port.in.BatchOperationHistoryQuery;
import org.craft.mcs.cap.core.traceability.application.port.out.BatchTraceabilityQueryStore;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepType;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubjectType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class QueryBatchOperationHistoryServiceTest {

  @ParameterizedTest
  @EnumSource(BatchTraceSubjectType.class)
  void excludesOnlyTheSubjectsOwnCreationStep(BatchTraceSubjectType subjectType) {
    final var queryStore = mock(BatchTraceabilityQueryStore.class);
    final var subjectId = newUuid();
    final var creationStepType = creationStepType(subjectType);
    final var service = new QueryBatchOperationHistoryService(queryStore);
    when(queryStore.hasOperationStep(subjectType, subjectId, creationStepType)).thenReturn(true);

    assertThat(service.hasOperations(new BatchOperationHistoryQuery(subjectType, subjectId)))
        .isTrue();
    verify(queryStore).hasOperationStep(subjectType, subjectId, creationStepType);
  }

  private static BatchTraceStepType creationStepType(BatchTraceSubjectType subjectType) {
    return switch (subjectType) {
      case FEEDSTOCK_BATCH -> BatchTraceStepType.FEEDSTOCK_BATCH_CREATED;
      case PRODUCTION_BATCH -> BatchTraceStepType.PRODUCTION_BATCH_CREATED;
    };
  }
}
