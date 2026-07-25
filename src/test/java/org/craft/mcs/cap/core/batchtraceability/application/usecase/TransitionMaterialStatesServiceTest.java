package org.craft.mcs.cap.core.batchtraceability.application.usecase;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.craft.mcs.cap.core.batchtraceability.application.model.MaterialStateAdvance;
import org.craft.mcs.cap.core.batchtraceability.application.model.MaterialStateInitialization;
import org.craft.mcs.cap.core.batchtraceability.application.model.MaterialStatesTransition;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityAccessControl;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityStore;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.CurrentMaterialStateStore;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceActorReference;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepType;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;


class TransitionMaterialStatesServiceTest {

  @Test
  void advancesExistingSubjectsInStableOrderAndInitializesNewOutput() {
    final var traceabilityStore = mock(BatchTraceabilityStore.class);
    final var currentStateStore = mock(CurrentMaterialStateStore.class);
    final var accessControl = mock(BatchTraceabilityAccessControl.class);
    final var service = new TransitionMaterialStatesService(
        traceabilityStore,
        currentStateStore,
        accessControl
    );
    final var workspaceId = WorkspaceId.newId();
    final var first = subject("00000000-0000-0000-0000-000000000001");
    final var second = subject("00000000-0000-0000-0000-000000000002");
    final var production = productionSubject("00000000-0000-0000-0000-000000000003");
    final var firstExpected = MaterialStateId.newId();
    final var firstNext = MaterialStateId.newId();
    final var secondExpected = MaterialStateId.newId();
    final var secondNext = MaterialStateId.newId();
    final var productionInitial = MaterialStateId.newId();
    final var traceStep = step(
        workspaceId,
        List.of(
            state(secondExpected, second),
            state(firstExpected, first)
        ),
        List.of(
            state(secondNext, second),
            state(firstNext, first),
            state(productionInitial, production)
        )
    );

    service.transition(new MaterialStatesTransition(
        traceStep,
        List.of(
            new MaterialStateAdvance(second, secondExpected, secondNext),
            new MaterialStateAdvance(first, firstExpected, firstNext)
        ),
        List.of(new MaterialStateInitialization(production, productionInitial))
    ));

    final var ordered = inOrder(accessControl, currentStateStore, traceabilityStore);
    ordered.verify(accessControl).requireWriteAccess(
        workspaceId,
        Set.of(BatchTraceSubjectType.FEEDSTOCK_BATCH, BatchTraceSubjectType.PRODUCTION_BATCH)
    );
    ordered.verify(currentStateStore).compareAndSet(workspaceId, first, firstExpected, firstNext);
    ordered.verify(currentStateStore).compareAndSet(workspaceId, second, secondExpected, secondNext);
    ordered.verify(currentStateStore).initialize(workspaceId, production, productionInitial);
    ordered.verify(traceabilityStore).append(traceStep);
  }

  @Test
  void rejectsAdvanceMissingFromTraceStep() {
    final var workspaceId = WorkspaceId.newId();
    final var source = subject("00000000-0000-0000-0000-000000000001");
    final var expected = MaterialStateId.newId();
    final var next = MaterialStateId.newId();
    final var unrelated = MaterialStateId.newId();
    final var traceStep = step(
        workspaceId,
        List.of(state(unrelated, source)),
        List.of(state(next, source))
    );

    assertThatThrownBy(() -> new MaterialStatesTransition(
        traceStep,
        List.of(new MaterialStateAdvance(source, expected, next)),
        List.of()
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessageContaining("matching trace input and output");
  }

  @Test
  void rejectsTraceStateWithoutDeclaredPointerTransition() {
    final var workspaceId = WorkspaceId.newId();
    final var source = subject("00000000-0000-0000-0000-000000000001");
    final var unrelated = subject("00000000-0000-0000-0000-000000000002");
    final var expected = MaterialStateId.newId();
    final var next = MaterialStateId.newId();
    final var traceStep = step(
        workspaceId,
        List.of(state(expected, source)),
        List.of(
            state(next, source),
            state(MaterialStateId.newId(), unrelated)
        )
    );

    assertThatThrownBy(() -> new MaterialStatesTransition(
        traceStep,
        List.of(new MaterialStateAdvance(source, expected, next)),
        List.of()
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessageContaining("exactly match");
  }

  @Test
  void rejectsOutputThatReusesAnotherInputStateId() {
    final var workspaceId = WorkspaceId.newId();
    final var firstSource = subject("00000000-0000-0000-0000-000000000001");
    final var secondSource = subject("00000000-0000-0000-0000-000000000002");
    final var firstExpected = MaterialStateId.newId();
    final var secondExpected = MaterialStateId.newId();
    final var firstNext = MaterialStateId.newId();
    final var traceStep = step(
        workspaceId,
        List.of(
            state(firstExpected, firstSource),
            state(secondExpected, secondSource)
        ),
        List.of(
            state(firstNext, firstSource),
            state(firstExpected, secondSource)
        )
    );

    assertThatThrownBy(() -> new MaterialStatesTransition(
        traceStep,
        List.of(
            new MaterialStateAdvance(firstSource, firstExpected, firstNext),
            new MaterialStateAdvance(secondSource, secondExpected, firstExpected)
        ),
        List.of()
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessageContaining("new state ids");
  }

  @Test
  void rejectsTransitionWithoutChangedSubjects() {
    final var workspaceId = WorkspaceId.newId();
    final var output = state(MaterialStateId.newId(), productionSubject(
        "00000000-0000-0000-0000-000000000003"
    ));

    assertThatThrownBy(() -> new MaterialStatesTransition(
        step(workspaceId, List.of(), List.of(output)),
        List.of(),
        List.of()
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("material state transition must change at least one subject");
  }

  @Test
  void rejectsSubjectDeclaredMoreThanOnce() {
    final var workspaceId = WorkspaceId.newId();
    final var source = subject("00000000-0000-0000-0000-000000000001");
    final var firstExpected = MaterialStateId.newId();
    final var secondExpected = MaterialStateId.newId();
    final var firstNext = MaterialStateId.newId();
    final var secondNext = MaterialStateId.newId();
    final var traceStep = step(
        workspaceId,
        List.of(state(firstExpected, source), state(secondExpected, source)),
        List.of(state(firstNext, source), state(secondNext, source))
    );

    assertThatThrownBy(() -> new MaterialStatesTransition(
        traceStep,
        List.of(
            new MaterialStateAdvance(source, firstExpected, firstNext),
            new MaterialStateAdvance(source, secondExpected, secondNext)
        ),
        List.of()
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("material state transition must declare each subject exactly once");
  }

  @Test
  void advanceMustCreateNewStateIdentity() {
    final var stateId = MaterialStateId.newId();

    assertThatThrownBy(() -> new MaterialStateAdvance(
        subject("00000000-0000-0000-0000-000000000001"),
        stateId,
        stateId
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("material state transition must create a new state");
  }

  private static BatchTraceStep step(
      WorkspaceId workspaceId,
      List<BatchTraceStepSubject> inputs,
      List<BatchTraceStepSubject> outputs
  ) {
    return BatchTraceStep.create(
        workspaceId,
        null,
        BatchTraceStepType.PRODUCTION_BATCH_CREATED,
        inputs,
        outputs,
        Instant.parse("2026-05-18T10:15:30Z"),
        BatchTraceActorReference.system("test"),
        null
    );
  }

  private static BatchTraceStepSubject state(
      MaterialStateId stateId,
      BatchTraceSubject subject
  ) {
    return new BatchTraceStepSubject(stateId, subject, null);
  }

  private static BatchTraceSubject subject(String id) {
    return BatchTraceSubject.of(BatchTraceSubjectType.FEEDSTOCK_BATCH, UUID.fromString(id));
  }

  private static BatchTraceSubject productionSubject(String id) {
    return BatchTraceSubject.of(BatchTraceSubjectType.PRODUCTION_BATCH, UUID.fromString(id));
  }
}
