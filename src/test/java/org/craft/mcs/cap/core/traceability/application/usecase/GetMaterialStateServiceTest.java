package org.craft.mcs.cap.core.traceability.application.usecase;

import java.util.Optional;
import org.craft.mcs.cap.core.traceability.application.MaterialStateNotFoundException;
import org.craft.mcs.cap.core.traceability.application.model.MaterialStateSnapshot;
import org.craft.mcs.cap.core.traceability.application.port.out.BatchTraceabilityAccessControl;
import org.craft.mcs.cap.core.traceability.application.port.out.MaterialStateHistoryStore;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.testing.BatchTraceSubjects.subject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class GetMaterialStateServiceTest {

  @Test
  void returnsAuthorizedImmutableState() {
    final var historyStore = mock(MaterialStateHistoryStore.class);
    final var accessControl = mock(BatchTraceabilityAccessControl.class);
    final var service = new GetMaterialStateService(historyStore, accessControl);
    final var workspaceId = WorkspaceId.newId();
    final var materialStateId = MaterialStateId.newId();
    final var state = new MaterialStateSnapshot(
        workspaceId,
        new BatchTraceStepSubject(
            materialStateId,
            subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-1"),
            null
        )
    );
    when(historyStore.find(materialStateId)).thenReturn(Optional.of(state));

    assertThat(service.get(materialStateId)).isEqualTo(state);
    verify(accessControl).requireReadAccess(workspaceId);
  }

  @Test
  void rejectsUnknownHistoricalState() {
    final var historyStore = mock(MaterialStateHistoryStore.class);
    final var service = new GetMaterialStateService(
        historyStore,
        mock(BatchTraceabilityAccessControl.class)
    );
    final var materialStateId = MaterialStateId.newId();
    when(historyStore.find(materialStateId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.get(materialStateId))
        .isInstanceOf(MaterialStateNotFoundException.class);
  }
}
