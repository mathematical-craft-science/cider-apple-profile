package org.craft.mcs.cap.core.traceability.application.usecase;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.craft.mcs.cap.core.traceability.application.contract.MaterialStateLineage;
import org.craft.mcs.cap.core.traceability.application.contract.MaterialStateLineageEntry;
import org.craft.mcs.cap.core.traceability.application.port.out.BatchTraceabilityAccessControl;
import org.craft.mcs.cap.core.traceability.application.port.out.MaterialStateLineageStore;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


class QueryMaterialStateLineageServiceTest {

  @Test
  void authorizesAndReturnsTraceabilityOwnedLineage() {
    final var workspaceId = WorkspaceId.newId();
    final var stateId = MaterialStateId.newId();
    final var subject = BatchTraceSubject.of(
        BatchTraceSubjectType.PRODUCTION_BATCH,
        UUID.fromString("019faecd-034f-7000-8000-000000000004")
    );
    final var currentStates = Map.of(subject, stateId);
    final var lineage = new MaterialStateLineage(
        subject,
        stateId,
        List.of(new MaterialStateLineageEntry(stateId, 0, Set.of()))
    );
    final var lineageStore = mock(MaterialStateLineageStore.class);
    final var accessControl = mock(BatchTraceabilityAccessControl.class);
    final var service = new QueryMaterialStateLineageService(lineageStore, accessControl);
    when(lineageStore.findLineages(workspaceId, currentStates)).thenReturn(Map.of(subject, lineage));

    assertThat(service.findLineages(workspaceId, currentStates))
        .containsExactlyEntriesOf(Map.of(subject, lineage));
    verify(accessControl).requireReadAccess(workspaceId);
  }

  @Test
  void authorizesEmptyQueryWithoutCallingPersistence() {
    final var workspaceId = WorkspaceId.newId();
    final var lineageStore = mock(MaterialStateLineageStore.class);
    final var accessControl = mock(BatchTraceabilityAccessControl.class);
    final var service = new QueryMaterialStateLineageService(lineageStore, accessControl);

    assertThat(service.findLineages(workspaceId, Map.of())).isEmpty();
    verify(accessControl).requireReadAccess(workspaceId);
    verifyNoInteractions(lineageStore);
  }
}
