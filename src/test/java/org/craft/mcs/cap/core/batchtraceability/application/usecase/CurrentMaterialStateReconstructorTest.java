package org.craft.mcs.cap.core.batchtraceability.application.usecase;

import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.core.batchtraceability.application.InconsistentMaterialStateHistoryException;
import org.craft.mcs.cap.core.batchtraceability.application.model.CurrentMaterialState;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceActorReference;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceMaterialSnapshot;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepType;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.testing.BatchTraceSubjects.subject;


class CurrentMaterialStateReconstructorTest {

  private static final Instant OCCURRED_AT = Instant.parse("2026-07-25T01:00:00Z");

  @Test
  void reconstructsTerminalStatesAndTransitionVersions() {
    final var workspaceId = WorkspaceId.newId();
    final var feedstock = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-1");
    final var production = subject(BatchTraceSubjectType.PRODUCTION_BATCH, "production-1");
    final var initialFeedstockState = state(MaterialStateId.newId(), feedstock, "100");
    final var remainingFeedstockState = state(MaterialStateId.newId(), feedstock, "40");
    final var productionState = state(MaterialStateId.newId(), production, "60");
    final var creation = step(
        workspaceId,
        BatchTraceStepType.FEEDSTOCK_BATCH_CREATED,
        List.of(),
        List.of(initialFeedstockState)
    );
    final var consumption = step(
        workspaceId,
        BatchTraceStepType.PRODUCTION_BATCH_CREATED,
        List.of(initialFeedstockState),
        List.of(remainingFeedstockState, productionState)
    );

    final var reconstructed = CurrentMaterialStateReconstructor.reconstruct(
        workspaceId,
        List.of(creation, consumption)
    );

    assertThat(reconstructed)
        .hasSize(2)
        .anySatisfy(state -> assertThat(state)
            .returns(feedstock.id(), current -> current.subject().id())
            .returns(remainingFeedstockState.traceStateId(), CurrentMaterialState::materialStateId)
            .returns(1L, CurrentMaterialState::version))
        .anySatisfy(state -> assertThat(state)
            .returns(production.id(), current -> current.subject().id())
            .returns(productionState.traceStateId(), CurrentMaterialState::materialStateId)
            .returns(0L, CurrentMaterialState::version));
  }

  @Test
  void rejectsTraceInputWithoutSuccessorStateForTheSameSubject() {
    final var workspaceId = WorkspaceId.newId();
    final var feedstock = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-1");
    final var production = subject(BatchTraceSubjectType.PRODUCTION_BATCH, "production-1");
    final var initialFeedstockState = state(MaterialStateId.newId(), feedstock, "100");

    assertThatThrownBy(() -> CurrentMaterialStateReconstructor.reconstruct(
        workspaceId,
        List.of(
            step(
                workspaceId,
                BatchTraceStepType.FEEDSTOCK_BATCH_CREATED,
                List.of(),
                List.of(initialFeedstockState)
            ),
            step(
                workspaceId,
                BatchTraceStepType.PRODUCTION_BATCH_CREATED,
                List.of(initialFeedstockState),
                List.of(state(MaterialStateId.newId(), production, "100"))
            )
        )
    ))
        .isInstanceOf(InconsistentMaterialStateHistoryException.class)
        .hasMessageContaining("exactly one output per subject");
  }

  @Test
  void rejectsBranchingStateHistory() {
    final var workspaceId = WorkspaceId.newId();
    final var feedstock = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-1");
    final var initial = state(MaterialStateId.newId(), feedstock, "100");
    final var firstNext = state(MaterialStateId.newId(), feedstock, "70");
    final var secondNext = state(MaterialStateId.newId(), feedstock, "60");

    assertThatThrownBy(() -> CurrentMaterialStateReconstructor.reconstruct(
        workspaceId,
        List.of(
            step(
                workspaceId,
                BatchTraceStepType.FEEDSTOCK_BATCH_CREATED,
                List.of(),
                List.of(initial)
            ),
            step(
                workspaceId,
                BatchTraceStepType.PRODUCTION_BATCH_CREATED,
                List.of(initial),
                List.of(firstNext)
            ),
            step(
                workspaceId,
                BatchTraceStepType.PRODUCTION_BATCH_CREATED,
                List.of(initial),
                List.of(secondNext)
            )
        )
    ))
        .isInstanceOf(InconsistentMaterialStateHistoryException.class)
        .hasMessageContaining("more than one transition");
  }

  @Test
  void rejectsDisconnectedStateHistory() {
    final var workspaceId = WorkspaceId.newId();
    final var feedstock = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-1");

    assertThatThrownBy(() -> CurrentMaterialStateReconstructor.reconstruct(
        workspaceId,
        List.of(
            step(
                workspaceId,
                BatchTraceStepType.FEEDSTOCK_BATCH_CREATED,
                List.of(),
                List.of(state(MaterialStateId.newId(), feedstock, "100"))
            ),
            step(
                workspaceId,
                BatchTraceStepType.FEEDSTOCK_BATCH_CREATED,
                List.of(),
                List.of(state(MaterialStateId.newId(), feedstock, "80"))
            )
        )
    ))
        .isInstanceOf(InconsistentMaterialStateHistoryException.class)
        .hasMessageContaining("one initial state and one terminal state");
  }

  private static BatchTraceStep step(
      WorkspaceId workspaceId,
      BatchTraceStepType stepType,
      List<BatchTraceStepSubject> inputs,
      List<BatchTraceStepSubject> outputs
  ) {
    return BatchTraceStep.create(
        workspaceId,
        null,
        stepType,
        inputs,
        outputs,
        OCCURRED_AT,
        BatchTraceActorReference.system("test"),
        null
    );
  }

  private static BatchTraceStepSubject state(
      MaterialStateId stateId,
      BatchTraceSubject subject,
      String volume
  ) {
    return new BatchTraceStepSubject(
        stateId,
        subject,
        BatchTraceMaterialSnapshot.ofVolume(VolumeValue.ofLiters(volume))
    );
  }
}
