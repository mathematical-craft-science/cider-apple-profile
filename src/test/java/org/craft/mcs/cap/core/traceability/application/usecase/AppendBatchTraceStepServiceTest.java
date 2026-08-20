package org.craft.mcs.cap.core.traceability.application.usecase;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.craft.mcs.cap.core.traceability.application.MaterialStateNotFoundException;
import org.craft.mcs.cap.core.traceability.application.contract.MaterialStateSnapshot;
import org.craft.mcs.cap.core.traceability.application.port.out.BatchTraceabilityAccessControl;
import org.craft.mcs.cap.core.traceability.application.port.out.BatchTraceabilityStore;
import org.craft.mcs.cap.core.traceability.application.port.out.MaterialStateHistoryStore;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepType;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.identity.ActorReference;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class AppendBatchTraceStepServiceTest {

  private static final WorkspaceId WORKSPACE_ID = WorkspaceId.newId();
  private static final BatchTraceSubject SUBJECT = BatchTraceSubject.of(
      BatchTraceSubjectType.FEEDSTOCK_BATCH,
      java.util.UUID.fromString("019fcb8d-a289-7000-8000-000000000001")
  );

  @Test
  void appendsTransitionOnlyWhenEveryInputIsAnAuthoritativeStateOfTheSameSubject() {
    final var input = BatchTraceStepSubject.reference(MaterialStateId.newId(), SUBJECT);
    final var step = step(input);
    final var store = mock(BatchTraceabilityStore.class);
    final var history = mock(MaterialStateHistoryStore.class);
    final var accessControl = mock(BatchTraceabilityAccessControl.class);
    when(history.find(input.materialStateId()))
        .thenReturn(Optional.of(new MaterialStateSnapshot(WORKSPACE_ID, input)));
    final var service = new AppendBatchTraceStepService(store, history, accessControl);

    service.append(step);

    verify(store).append(step);
  }

  @Test
  void rejectsInputThatWasNeverProducedByTraceability() {
    final var input = BatchTraceStepSubject.reference(MaterialStateId.newId(), SUBJECT);
    final var step = step(input);
    final var store = mock(BatchTraceabilityStore.class);
    final var history = mock(MaterialStateHistoryStore.class);
    when(history.find(input.materialStateId())).thenReturn(Optional.empty());
    final var service = new AppendBatchTraceStepService(
        store,
        history,
        mock(BatchTraceabilityAccessControl.class)
    );

    assertThatThrownBy(() -> service.append(step))
        .isInstanceOf(MaterialStateNotFoundException.class);
    verify(store, never()).append(step);
  }

  @Test
  void rejectsInputStateReusedForAnotherWorkspaceOrBusinessSubject() {
    final var input = BatchTraceStepSubject.reference(MaterialStateId.newId(), SUBJECT);
    final var step = step(input);
    final var store = mock(BatchTraceabilityStore.class);
    final var history = mock(MaterialStateHistoryStore.class);
    when(history.find(input.materialStateId())).thenReturn(Optional.of(new MaterialStateSnapshot(
        WorkspaceId.newId(),
        BatchTraceStepSubject.reference(input.materialStateId(), SUBJECT)
    )));
    final var service = new AppendBatchTraceStepService(
        store,
        history,
        mock(BatchTraceabilityAccessControl.class)
    );

    assertThatThrownBy(() -> service.append(step))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("trace input state does not belong to the supplied workspace and material subject");
    verify(store, never()).append(step);
  }

  private static BatchTraceStep step(BatchTraceStepSubject input) {
    return BatchTraceStep.create(
        WORKSPACE_ID,
        null,
        BatchTraceStepType.PRODUCTION_BATCH_CREATED,
        List.of(input),
        List.of(BatchTraceStepSubject.reference(MaterialStateId.newId(), BatchTraceSubject.of(
            BatchTraceSubjectType.PRODUCTION_BATCH,
            java.util.UUID.fromString("019fcb8d-a289-7000-8000-000000000002")
        ))),
        Instant.parse("2026-08-10T08:00:00Z"),
        ActorReference.machineIdentity(java.util.UUID.fromString("019fcb8d-a289-7000-8000-000000000003")),
        null
    );
  }
}
