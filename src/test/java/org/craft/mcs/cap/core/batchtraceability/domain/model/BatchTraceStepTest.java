package org.craft.mcs.cap.core.batchtraceability.domain.model;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class BatchTraceStepTest {

  private static final WorkspaceId WORKSPACE_ID = WorkspaceId.newId();
  private static final BatchTraceActorReference ACTOR = BatchTraceActorReference.system("test");

  @Test
  void createsAppendOnlyTraceStepWithDefensiveCopies() {
    final var outputs = new ArrayList<BatchTraceStepSubject>();
    outputs.add(BatchTraceStepSubject.of(subject("production-1")));

    final var step = BatchTraceStep.create(
        WORKSPACE_ID,
        null,
        BatchTraceOperationType.CREATE_PRODUCTION_BATCH,
        List.of(BatchTraceStepSubject.of(subject("feedstock-1"))),
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
        BatchTraceOperationType.CREATE_PRODUCTION_BATCH,
        List.of(BatchTraceStepSubject.of(subject("feedstock-1"))),
        List.of(BatchTraceStepSubject.of(subject("production-1"))),
        Instant.parse("2026-05-18T00:00:00Z"),
        ACTOR,
        null
    );

    assertThatThrownBy(() -> step.outputs().add(BatchTraceStepSubject.of(subject("production-2"))))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void rejectsStepWithoutOutputSubject() {
    assertThatThrownBy(() -> BatchTraceStep.create(
        WORKSPACE_ID,
        null,
        BatchTraceOperationType.CREATE_PRODUCTION_BATCH,
        List.of(BatchTraceStepSubject.of(subject("feedstock-1"))),
        List.of(),
        Instant.parse("2026-05-18T00:00:00Z"),
        ACTOR,
        null
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessageContaining("output subject");
  }

  @Test
  void supportsSeveralProductionStepsFromSameFeedstockSubject() {
    final var feedstock = BatchTraceStepSubject.of(subject("feedstock-1"));

    final var firstProduction = BatchTraceStep.create(
        WORKSPACE_ID,
        null,
        BatchTraceOperationType.CREATE_PRODUCTION_BATCH,
        List.of(feedstock),
        List.of(BatchTraceStepSubject.of(new BatchTraceSubject(
            BatchTraceSubjectType.PRODUCTION_BATCH,
            "production-1",
            null
        ))),
        Instant.parse("2026-05-18T00:00:00Z"),
        ACTOR,
        null
    );
    final var secondProduction = BatchTraceStep.create(
        WORKSPACE_ID,
        null,
        BatchTraceOperationType.CREATE_PRODUCTION_BATCH,
        List.of(feedstock),
        List.of(BatchTraceStepSubject.of(new BatchTraceSubject(
            BatchTraceSubjectType.PRODUCTION_BATCH,
            "production-2",
            null
        ))),
        Instant.parse("2026-05-18T01:00:00Z"),
        ACTOR,
        null
    );

    assertThat(firstProduction.inputs()).containsExactly(feedstock);
    assertThat(secondProduction.inputs()).containsExactly(feedstock);
    assertThat(firstProduction.outputs()).isNotEqualTo(secondProduction.outputs());
  }

  @Test
  void rejectsBlankSubjectId() {
    assertThatThrownBy(() -> BatchTraceSubject.of(BatchTraceSubjectType.FEEDSTOCK_BATCH, " "))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessageContaining("subject id");
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

  private static BatchTraceSubject subject(String id) {
    return new BatchTraceSubject(BatchTraceSubjectType.FEEDSTOCK_BATCH, id, "Source " + id);
  }
}
