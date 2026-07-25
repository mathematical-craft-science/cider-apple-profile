package org.craft.mcs.cap.core.batchtraceability.application.usecase;

import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityAccessControl;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityStore;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.CurrentMaterialStateStore;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceActorReference;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceMaterialSnapshot;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepType;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.testing.BatchTraceSubjects.subject;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;


class InitializeMaterialStateServiceTest {

  private static final Instant OCCURRED_AT = Instant.parse("2026-07-25T01:00:00Z");

  @Test
  void initializesPointerAfterAppendingCreationStep() {
    final var traceabilityStore = mock(BatchTraceabilityStore.class);
    final var currentStateStore = mock(CurrentMaterialStateStore.class);
    final var service = service(traceabilityStore, currentStateStore);
    final var workspaceId = WorkspaceId.newId();
    final var subject = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-1");
    final var initialStateId = MaterialStateId.newId();
    final var step = creationStep(workspaceId, state(initialStateId, subject));

    service.initialize(step, subject, initialStateId);

    final var ordered = inOrder(traceabilityStore, currentStateStore);
    ordered.verify(traceabilityStore).append(step);
    ordered.verify(currentStateStore).initialize(workspaceId, subject, initialStateId);
  }

  @Test
  void rejectsCreationStepWithoutDeclaredInitialState() {
    final var traceabilityStore = mock(BatchTraceabilityStore.class);
    final var currentStateStore = mock(CurrentMaterialStateStore.class);
    final var service = service(traceabilityStore, currentStateStore);
    final var workspaceId = WorkspaceId.newId();
    final var subject = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-1");
    final var expectedStateId = MaterialStateId.newId();
    final var step = creationStep(workspaceId, state(MaterialStateId.newId(), subject));

    assertThatThrownBy(() -> service.initialize(step, subject, expectedStateId))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("creation output must reference the declared material subject and state");
    verify(traceabilityStore, never()).append(step);
    verify(currentStateStore, never()).initialize(workspaceId, subject, expectedStateId);
  }

  private static InitializeMaterialStateService service(
      BatchTraceabilityStore traceabilityStore,
      CurrentMaterialStateStore currentStateStore
  ) {
    return new InitializeMaterialStateService(
        traceabilityStore,
        currentStateStore,
        mock(BatchTraceabilityAccessControl.class)
    );
  }

  private static BatchTraceStep creationStep(
      WorkspaceId workspaceId,
      BatchTraceStepSubject output
  ) {
    return BatchTraceStep.create(
        workspaceId,
        null,
        BatchTraceStepType.FEEDSTOCK_BATCH_CREATED,
        List.of(),
        List.of(output),
        OCCURRED_AT,
        BatchTraceActorReference.system("test"),
        null
    );
  }

  private static BatchTraceStepSubject state(
      MaterialStateId stateId,
      BatchTraceSubject subject
  ) {
    return new BatchTraceStepSubject(
        stateId,
        subject,
        BatchTraceMaterialSnapshot.ofVolume(VolumeValue.ofLiters("100"))
    );
  }
}
