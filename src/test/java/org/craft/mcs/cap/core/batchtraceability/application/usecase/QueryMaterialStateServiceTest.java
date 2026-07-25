package org.craft.mcs.cap.core.batchtraceability.application.usecase;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.craft.mcs.cap.core.batchtraceability.application.model.CurrentMaterialState;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityAccessControl;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.CurrentMaterialStateStore;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.testing.BatchTraceSubjects.subject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class QueryMaterialStateServiceTest {

  @Test
  void requiresExpectedCurrentStateUnderConsistencyGuard() {
    final var currentStateStore = mock(CurrentMaterialStateStore.class);
    final var accessControl = mock(BatchTraceabilityAccessControl.class);
    final var service = new QueryMaterialStateService(currentStateStore, accessControl);
    final var workspaceId = WorkspaceId.newId();
    final var subject = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-1");
    final var expectedStateId = MaterialStateId.newId();
    final var current = new CurrentMaterialState(workspaceId, subject, expectedStateId, 2);
    when(currentStateStore.lockCurrent(subject)).thenReturn(Optional.of(current));

    final var result = service.requireCurrent(subject, expectedStateId);

    assertThat(result).isEqualTo(current);
    verify(accessControl).requireReadAccess(workspaceId);
  }

  @Test
  void rejectsStaleStateUnderConsistencyGuard() {
    final var currentStateStore = mock(CurrentMaterialStateStore.class);
    final var service = service(currentStateStore);
    final var workspaceId = WorkspaceId.newId();
    final var subject = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-1");
    final var expectedStateId = MaterialStateId.newId();
    when(currentStateStore.lockCurrent(subject)).thenReturn(Optional.of(
        new CurrentMaterialState(workspaceId, subject, MaterialStateId.newId(), 3)
    ));

    assertThatThrownBy(() -> service.requireCurrent(subject, expectedStateId))
        .isInstanceOf(ConcurrentResourceModificationException.class);
  }

  @Test
  void resolvesBulkCurrentStateByBusinessIdentityInsteadOfDisplayNameSnapshot() {
    final var currentStateStore = mock(CurrentMaterialStateStore.class);
    final var service = service(currentStateStore);
    final var workspaceId = WorkspaceId.newId();
    final var persistedSubject = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-1");
    final var requestedSubject = new BatchTraceSubject(
        persistedSubject.type(),
        persistedSubject.id(),
        "Current display name"
    );
    final var current = new CurrentMaterialState(
        workspaceId,
        persistedSubject,
        MaterialStateId.newId(),
        2
    );
    when(currentStateStore.findCurrent(workspaceId, Set.of(requestedSubject)))
        .thenReturn(Map.of(persistedSubject, current));

    assertThat(service.getCurrent(workspaceId, Set.of(requestedSubject)))
        .containsOnlyKeys(requestedSubject)
        .containsEntry(requestedSubject, current);
  }

  private static QueryMaterialStateService service(CurrentMaterialStateStore currentStateStore) {
    return new QueryMaterialStateService(
        currentStateStore,
        mock(BatchTraceabilityAccessControl.class)
    );
  }
}
