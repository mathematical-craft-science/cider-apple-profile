package org.craft.mcs.cap.core.batchtraceability.application.usecase;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.craft.mcs.cap.core.batchtraceability.application.model.CurrentMaterialState;
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
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.testing.BatchTraceSubjects.subject;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class MaterialStateServiceTest {

  private static final Instant OCCURRED_AT = Instant.parse("2026-07-25T01:00:00Z");

  @Test
  void initializesPointerAfterAppendingCreationStep() {
    final var traceabilityStore = mock(BatchTraceabilityStore.class);
    final var currentStateStore = mock(CurrentMaterialStateStore.class);
    final var service = service(traceabilityStore, currentStateStore);
    final var workspaceId = WorkspaceId.newId();
    final var subject = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-1");
    final var initialStateId = MaterialStateId.newId();
    final var step = creationStep(
        workspaceId,
        subject,
        state(initialStateId, subject)
    );

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
    final var step = creationStep(
        workspaceId,
        subject,
        state(MaterialStateId.newId(), subject)
    );

    assertThatThrownBy(() -> service.initialize(step, subject, expectedStateId))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("creation output must reference the declared material subject and state");
    verify(traceabilityStore, never()).append(step);
    verify(currentStateStore, never()).initialize(workspaceId, subject, expectedStateId);
  }

  @Test
  void requiresExpectedCurrentStateUnderConsistencyGuard() {
    final var traceabilityStore = mock(BatchTraceabilityStore.class);
    final var currentStateStore = mock(CurrentMaterialStateStore.class);
    final var accessControl = mock(BatchTraceabilityAccessControl.class);
    final var service = new MaterialStateService(traceabilityStore, currentStateStore, accessControl);
    final var workspaceId = WorkspaceId.newId();
    final var subject = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-1");
    final var expectedStateId = MaterialStateId.newId();
    final var current = new CurrentMaterialState(workspaceId, subject, expectedStateId, 2);
    when(currentStateStore.findCurrentForConsistency(subject)).thenReturn(Optional.of(current));

    final var result = service.requireCurrent(subject, expectedStateId);

    assertThat(result).isEqualTo(current);
    verify(accessControl).requireReadAccess(workspaceId);
  }

  @Test
  void rejectsStaleStateUnderConsistencyGuard() {
    final var currentStateStore = mock(CurrentMaterialStateStore.class);
    final var service = service(mock(BatchTraceabilityStore.class), currentStateStore);
    final var workspaceId = WorkspaceId.newId();
    final var subject = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-1");
    final var expectedStateId = MaterialStateId.newId();
    when(currentStateStore.findCurrentForConsistency(subject)).thenReturn(Optional.of(
        new CurrentMaterialState(workspaceId, subject, MaterialStateId.newId(), 3)
    ));

    assertThatThrownBy(() -> service.requireCurrent(subject, expectedStateId))
        .isInstanceOf(ConcurrentResourceModificationException.class);
  }

  private static MaterialStateService service(
      BatchTraceabilityStore traceabilityStore,
      CurrentMaterialStateStore currentStateStore
  ) {
    return new MaterialStateService(
        traceabilityStore,
        currentStateStore,
        mock(BatchTraceabilityAccessControl.class)
    );
  }

  private static BatchTraceStep creationStep(
      WorkspaceId workspaceId,
      BatchTraceSubject subject,
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
