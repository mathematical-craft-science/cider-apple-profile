package org.craft.mcs.cap.core.traceability.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.identity.ActorReference;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.testing.BatchTraceSubjects;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class BatchTraceStepTest {

  private static final WorkspaceId WORKSPACE_ID = WorkspaceId.newId();
  private static final ActorReference ACTOR = ActorReference.machineIdentity(
      BatchTraceSubjects.id("test-actor")
  );

  @Test
  void createsAppendOnlyTraceStepWithDefensiveCopies() {
    final var outputs = new ArrayList<BatchTraceStepSubject>();
    outputs.add(state(subject("production-1")));

    final var step = BatchTraceStep.create(
        WORKSPACE_ID,
        null,
        BatchTraceStepType.PRODUCTION_BATCH_CREATED,
        List.of(state(subject("feedstock-1"))),
        outputs,
        Instant.parse("2026-05-18T00:00:00Z"),
        ACTOR,
        " accepted input "
    );

    outputs.clear();

    assertThat(step)
        .returns(WORKSPACE_ID, BatchTraceStep::workspaceId)
        .returns(ACTOR, BatchTraceStep::performedBy)
        .returns("accepted input", BatchTraceStep::note);
    assertThat(step.outputs()).hasSize(1);
  }

  @Test
  void exposesOutputsAsImmutableList() {
    final var step = BatchTraceStep.create(
        WORKSPACE_ID,
        null,
        BatchTraceStepType.PRODUCTION_BATCH_CREATED,
        List.of(state(subject("feedstock-1"))),
        List.of(state(subject("production-1"))),
        Instant.parse("2026-05-18T00:00:00Z"),
        ACTOR,
        null
    );

    assertThatThrownBy(() -> step.outputs().add(state(subject("production-2"))))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void rejectsStepWithoutOutputSubject() {
    assertThatThrownBy(() -> BatchTraceStep.create(
        WORKSPACE_ID,
        null,
        BatchTraceStepType.PRODUCTION_BATCH_CREATED,
        List.of(state(subject("feedstock-1"))),
        List.of(),
        Instant.parse("2026-05-18T00:00:00Z"),
        ACTOR,
        null
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessageContaining("output subject");
  }

  @Test
  void rejectsRepeatedOutputStateWithinOneStep() {
    final var outputState = state(subject("production-1"));

    assertThatThrownBy(() -> BatchTraceStep.create(
        WORKSPACE_ID,
        null,
        BatchTraceStepType.PRODUCTION_BATCH_CREATED,
        List.of(state(subject("feedstock-1"))),
        List.of(outputState, outputState),
        Instant.parse("2026-05-18T00:00:00Z"),
        ACTOR,
        null
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("batch trace step output state ids must be unique");
  }

  @Test
  void supportsSuccessiveStatesOfTheSameFeedstockSubject() {
    final var feedstockSubject = subject("feedstock-1");
    final var firstFeedstockState = state(feedstockSubject);
    final var secondFeedstockState = state(feedstockSubject);

    final var firstProduction = BatchTraceStep.create(
        WORKSPACE_ID,
        null,
        BatchTraceStepType.PRODUCTION_BATCH_CREATED,
        List.of(firstFeedstockState),
        List.of(state(
            BatchTraceSubjects.subject(
                BatchTraceSubjectType.PRODUCTION_BATCH,
                "production-1"
            ))),
        Instant.parse("2026-05-18T00:00:00Z"),
        ACTOR,
        null
    );
    final var secondProduction = BatchTraceStep.create(
        WORKSPACE_ID,
        null,
        BatchTraceStepType.PRODUCTION_BATCH_CREATED,
        List.of(secondFeedstockState),
        List.of(state(
            BatchTraceSubjects.subject(
                BatchTraceSubjectType.PRODUCTION_BATCH,
                "production-2"
            ))),
        Instant.parse("2026-05-18T01:00:00Z"),
        ACTOR,
        null
    );

    assertThat(firstProduction.inputs()).containsExactly(firstFeedstockState);
    assertThat(secondProduction.inputs()).containsExactly(secondFeedstockState);
    assertThat(firstFeedstockState)
        .returns(feedstockSubject, BatchTraceStepSubject::subject)
        .satisfies(first -> assertThat(first.materialStateId())
            .isNotEqualTo(secondFeedstockState.materialStateId()));
    assertThat(firstProduction.outputs()).isNotEqualTo(secondProduction.outputs());
  }

  @Test
  void rejectsEmptyMaterialSnapshot() {
    assertThatThrownBy(() -> new BatchTraceMaterialSnapshot(null, null, null))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessageContaining("at least one value");
  }

  @Test
  void createsVolumeSnapshot() {
    final var snapshot = BatchTraceMaterialSnapshot.ofVolume(VolumeValue.ofLiters("25"));

    assertThat(snapshot.volume()).isNotNull();
    assertThat(snapshot.volume().liters()).isEqualByComparingTo("25");
  }

  @Test
  void allowsOnlyOutputStateToInvalidateMeasurementEvidence() {
    final var invalidatingInput = new BatchTraceStepSubject(
        MaterialStateId.newId(),
        subject("feedstock-1"),
        null,
        Set.of(MeasurementCharacteristic.PH)
    );

    assertThatThrownBy(() -> BatchTraceStep.create(
        WORKSPACE_ID,
        null,
        BatchTraceStepType.PRODUCTION_BATCH_CREATED,
        List.of(invalidatingInput),
        List.of(state(subject("feedstock-1"))),
        Instant.parse("2026-05-18T00:00:00Z"),
        ACTOR,
        null
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessageContaining("only by an output state");
  }

  @Test
  void rejectsRepeatedInputStateWithinOneStep() {
    final var inputState = state(subject("feedstock-1"));

    assertThatThrownBy(() -> BatchTraceStep.create(
        WORKSPACE_ID,
        null,
        BatchTraceStepType.PRODUCTION_BATCH_CREATED,
        List.of(inputState, inputState),
        List.of(state(subject("production-1"))),
        Instant.parse("2026-05-18T00:00:00Z"),
        ACTOR,
        null
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("batch trace step input state ids must be unique");
  }

  @Test
  void rejectsOutputThatReusesAnInputStateIdentity() {
    final var materialStateId = MaterialStateId.newId();

    assertThatThrownBy(() -> BatchTraceStep.create(
        WORKSPACE_ID,
        null,
        BatchTraceStepType.PRODUCTION_BATCH_CREATED,
        List.of(BatchTraceStepSubject.reference(materialStateId, subject("feedstock-1"))),
        List.of(BatchTraceStepSubject.reference(materialStateId, subject("production-1"))),
        Instant.parse("2026-05-18T00:00:00Z"),
        ACTOR,
        null
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("batch trace step outputs must identify new immutable material states");
  }

  @Test
  void rejectsSeveralInputStatesOfTheSameBusinessSubject() {
    final var feedstock = subject("feedstock-1");

    assertThatThrownBy(() -> BatchTraceStep.create(
        WORKSPACE_ID,
        null,
        BatchTraceStepType.PRODUCTION_BATCH_CREATED,
        List.of(state(feedstock), state(feedstock)),
        List.of(state(subject("production-1"))),
        Instant.parse("2026-05-18T00:00:00Z"),
        ACTOR,
        null
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("batch trace step input subjects must be unique");
  }

  @Test
  void rejectsSeveralOutputStatesOfTheSameBusinessSubject() {
    final var production = subject("production-1");

    assertThatThrownBy(() -> BatchTraceStep.create(
        WORKSPACE_ID,
        null,
        BatchTraceStepType.PRODUCTION_BATCH_CREATED,
        List.of(state(subject("feedstock-1"))),
        List.of(state(production), state(production)),
        Instant.parse("2026-05-18T00:00:00Z"),
        ACTOR,
        null
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("batch trace step output subjects must be unique");
  }

  private static BatchTraceStepSubject state(BatchTraceSubject subject) {
    return BatchTraceStepSubject.reference(MaterialStateId.newId(), subject);
  }

  private static BatchTraceSubject subject(String fixtureReference) {
    return new BatchTraceSubject(
        BatchTraceSubjectType.FEEDSTOCK_BATCH,
        BatchTraceSubjects.id(fixtureReference),
        "Source " + fixtureReference
    );
  }
}
