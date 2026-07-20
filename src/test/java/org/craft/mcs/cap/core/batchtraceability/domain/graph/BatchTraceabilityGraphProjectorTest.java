package org.craft.mcs.cap.core.batchtraceability.domain.graph;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceActorReference;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceMaterialSnapshot;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceOperationType;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStateId;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepId;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;


class BatchTraceabilityGraphProjectorTest {

  private static final WorkspaceId WORKSPACE_ID = WorkspaceId.newId();

  @Test
  void buildsSourceTreeFromSeveralImmutableStepsWithSameInputSubject() {
    final var feedstock = BatchTraceStepSubject.of(subject(
        BatchTraceSubjectType.FEEDSTOCK_BATCH,
        "feedstock-1"
    ));
    final var firstProduction = BatchTraceStepSubject.of(subject(
        BatchTraceSubjectType.PRODUCTION_BATCH,
        "production-1"
    ));
    final var secondProduction = BatchTraceStepSubject.of(subject(
        BatchTraceSubjectType.PRODUCTION_BATCH,
        "production-2"
    ));
    final var firstStep = step(feedstock, firstProduction, "2026-05-18T00:00:00Z");
    final var secondStep = step(feedstock, secondProduction, "2026-05-18T01:00:00Z");

    final var graph = BatchTraceabilityGraphProjector.sourceTreeFrom(List.of(firstStep, secondStep));

    assertThat(graph.nodes())
        .extracting(BatchTraceNode::subject)
        .containsExactly(feedstock.subject(), firstProduction.subject(), secondProduction.subject());
    assertThat(graph.edges()).hasSize(2);
    assertThat(graph.edges())
        .allSatisfy(edge -> assertThat(edge.inputs()).containsExactly(feedstock));
    assertThat(graph.edges())
        .extracting(edge -> edge.outputs().getFirst().subject().id())
        .containsExactly("production-1", "production-2");
    assertThat(graph.edges())
        .extracting(BatchTraceEdge::occurredAt)
        .containsExactly(
            Instant.parse("2026-05-18T00:00:00Z"),
            Instant.parse("2026-05-18T01:00:00Z")
        );
  }

  @Test
  void supportsSplitStepWithOneInputAndSeveralOutputs() {
    final var sourceProduction = BatchTraceStepSubject.of(subject(
        BatchTraceSubjectType.PRODUCTION_BATCH,
        "production-1"
    ));
    final var firstSplitPart = BatchTraceStepSubject.of(subject(
        BatchTraceSubjectType.PRODUCTION_BATCH,
        "production-1-a"
    ));
    final var secondSplitPart = BatchTraceStepSubject.of(subject(
        BatchTraceSubjectType.PRODUCTION_BATCH,
        "production-1-b"
    ));
    final var splitStep = BatchTraceStep.create(
        WORKSPACE_ID,
        null,
        BatchTraceOperationType.SPLIT,
        List.of(sourceProduction),
        List.of(firstSplitPart, secondSplitPart),
        Instant.parse("2026-05-18T02:00:00Z"),
        BatchTraceActorReference.system("test"),
        null
    );

    final var graph = BatchTraceabilityGraphProjector.sourceTreeFrom(List.of(splitStep));

    assertThat(graph.nodes())
        .extracting(BatchTraceNode::subject)
        .containsExactly(
            sourceProduction.subject(),
            firstSplitPart.subject(),
            secondSplitPart.subject()
        );
    assertThat(graph.edges()).singleElement()
        .satisfies(edge -> {
          assertThat(edge.operationType()).isEqualTo(BatchTraceOperationType.SPLIT);
          assertThat(edge.occurredAt()).isEqualTo(Instant.parse("2026-05-18T02:00:00Z"));
          assertThat(edge.inputs()).containsExactly(sourceProduction);
          assertThat(edge.outputs()).containsExactly(firstSplitPart, secondSplitPart);
        });
  }

  @Test
  void sortsEdgesByOccurredAtAndStepIdForTimelineRendering() {
    final var feedstock = BatchTraceStepSubject.of(subject(
        BatchTraceSubjectType.FEEDSTOCK_BATCH,
        "feedstock-1"
    ));
    final var laterStep = step(
        BatchTraceStepId.of(UUID.fromString("00000000-0000-0000-0000-000000000001")),
        feedstock,
        BatchTraceStepSubject.of(subject(BatchTraceSubjectType.PRODUCTION_BATCH, "production-later")),
        "2026-05-18T02:00:00Z"
    );
    final var secondSameTimeStep = step(
        BatchTraceStepId.of(UUID.fromString("00000000-0000-0000-0000-000000000003")),
        feedstock,
        BatchTraceStepSubject.of(subject(BatchTraceSubjectType.PRODUCTION_BATCH, "production-second")),
        "2026-05-18T01:00:00Z"
    );
    final var firstSameTimeStep = step(
        BatchTraceStepId.of(UUID.fromString("00000000-0000-0000-0000-000000000002")),
        feedstock,
        BatchTraceStepSubject.of(subject(BatchTraceSubjectType.PRODUCTION_BATCH, "production-first")),
        "2026-05-18T01:00:00Z"
    );

    final var graph = BatchTraceabilityGraphProjector.sourceTreeFrom(List.of(
        laterStep,
        secondSameTimeStep,
        firstSameTimeStep
    ));

    assertThat(graph.edges())
        .extracting(edge -> edge.outputs().getFirst().subject().id())
        .containsExactly("production-first", "production-second", "production-later");
  }

  @Test
  void preservesTemporalStatesOfTheSameProductionBatchAsDistinctNodes() {
    final var production = subject(BatchTraceSubjectType.PRODUCTION_BATCH, "production-1");
    final var initial = state(
        "019b24e0-3500-7000-8000-000000000001",
        production,
        "100",
        "1.060"
    );
    final var afterFirstRacking = state(
        "019b24e0-3500-7000-8000-000000000002",
        production,
        "92",
        "1.060"
    );
    final var afterMeasurement = state(
        "019b24e0-3500-7000-8000-000000000003",
        production,
        "92",
        "1.035"
    );
    final var afterSecondRacking = state(
        "019b24e0-3500-7000-8000-000000000004",
        production,
        "88",
        "1.035"
    );
    final var firstRacking = step(
        BatchTraceOperationType.RACKING,
        initial,
        afterFirstRacking,
        "2026-05-18T01:00:00Z"
    );
    final var measurement = step(
        BatchTraceOperationType.MEASUREMENT_ADDED,
        afterFirstRacking,
        afterMeasurement,
        "2026-05-18T02:00:00Z"
    );
    final var secondRacking = step(
        BatchTraceOperationType.RACKING,
        afterMeasurement,
        afterSecondRacking,
        "2026-05-18T03:00:00Z"
    );

    final var graph = BatchTraceabilityGraphProjector.sourceTreeFrom(List.of(
        firstRacking,
        measurement,
        secondRacking
    ));

    assertThat(graph.nodes())
        .extracting(
            node -> node.traceStateId().value().toString(),
            node -> node.subject().id(),
            node -> node.snapshot().volume().liters(),
            BatchTraceabilityGraphProjectorTest::sugarValue
        )
        .containsExactly(
            tuple("019b24e0-3500-7000-8000-000000000001", "production-1", new BigDecimal("100"),
                new BigDecimal("1.060")),
            tuple("019b24e0-3500-7000-8000-000000000002", "production-1", new BigDecimal("92"),
                new BigDecimal("1.060")),
            tuple("019b24e0-3500-7000-8000-000000000003", "production-1", new BigDecimal("92"),
                new BigDecimal("1.035")),
            tuple("019b24e0-3500-7000-8000-000000000004", "production-1", new BigDecimal("88"),
                new BigDecimal("1.035"))
        );
    assertThat(graph.edges())
        .extracting(
            BatchTraceEdge::operationType,
            edge -> edge.inputs().getFirst().traceStateId(),
            edge -> edge.outputs().getFirst().traceStateId()
        )
        .containsExactly(
            tuple(BatchTraceOperationType.RACKING, initial.traceStateId(), afterFirstRacking.traceStateId()),
            tuple(
                BatchTraceOperationType.MEASUREMENT_ADDED,
                afterFirstRacking.traceStateId(),
                afterMeasurement.traceStateId()
            ),
            tuple(
                BatchTraceOperationType.RACKING,
                afterMeasurement.traceStateId(),
                afterSecondRacking.traceStateId()
            )
        );
  }

  @Test
  void rejectsDifferentSnapshotsForTheSameTraceStateId() {
    final var production = subject(BatchTraceSubjectType.PRODUCTION_BATCH, "production-1");
    final var stateId = BatchTraceStateId.newId();
    final var initial = state(stateId, production, "100", "1.060");
    final var conflicting = state(stateId, production, "92", "1.060");
    final var firstStep = step(initial, BatchTraceStepSubject.of(production), "2026-05-18T01:00:00Z");
    final var secondStep = step(conflicting, BatchTraceStepSubject.of(production), "2026-05-18T02:00:00Z");
    final var steps = List.of(firstStep, secondStep);

    assertThatThrownBy(() -> BatchTraceabilityGraphProjector.sourceTreeFrom(steps))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("batch trace state id cannot identify different subjects or snapshots");
  }

  @Test
  void reusesStateIdentityWhenOperationDoesNotChangeMaterialState() {
    final var state = state(
        "019b24e0-3500-7000-8000-000000000005",
        subject(BatchTraceSubjectType.PRODUCTION_BATCH, "production-1"),
        "88",
        "1.035"
    );
    final var assessment = step(
        BatchTraceOperationType.FINAL_ASSESSMENT,
        state,
        state,
        "2026-05-18T04:00:00Z"
    );

    final var graph = BatchTraceabilityGraphProjector.sourceTreeFrom(List.of(assessment));

    assertThat(graph.nodes()).singleElement()
        .returns(state.traceStateId(), BatchTraceNode::traceStateId);
    assertThat(graph.edges()).singleElement()
        .satisfies(edge -> assertThat(edge)
            .extracting(
                value -> value.inputs().getFirst().traceStateId(),
                value -> value.outputs().getFirst().traceStateId()
            )
            .containsOnly(state.traceStateId()));
  }

  @Test
  void rejectsEdgeStateThatIsMissingFromGraphNodes() {
    final var input = BatchTraceStepSubject.of(subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-1"));
    final var output = BatchTraceStepSubject.of(subject(BatchTraceSubjectType.PRODUCTION_BATCH, "production-1"));
    final var edge = new BatchTraceEdge(
        BatchTraceStepId.newId(),
        BatchTraceOperationType.CREATE_PRODUCTION_BATCH,
        Instant.parse("2026-05-18T00:00:00Z"),
        List.of(input),
        List.of(output)
    );
    final var nodes = List.of(BatchTraceNode.from(output));
    final var edges = List.of(edge);

    assertThatThrownBy(() -> new BatchTraceabilityGraph(nodes, edges))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("batch trace edge state must be present in graph nodes");
  }

  private static BatchTraceStep step(
      BatchTraceStepSubject input,
      BatchTraceStepSubject output,
      String occurredAt
  ) {
    return step(BatchTraceStepId.newId(), input, output, occurredAt);
  }

  private static BatchTraceStep step(
      BatchTraceOperationType operationType,
      BatchTraceStepSubject input,
      BatchTraceStepSubject output,
      String occurredAt
  ) {
    return new BatchTraceStep(
        WORKSPACE_ID,
        BatchTraceStepId.newId(),
        null,
        operationType,
        List.of(input),
        List.of(output),
        Instant.parse(occurredAt),
        BatchTraceActorReference.system("test"),
        null
    );
  }

  private static BatchTraceStep step(
      BatchTraceStepId stepId,
      BatchTraceStepSubject input,
      BatchTraceStepSubject output,
      String occurredAt
  ) {
    return new BatchTraceStep(
        WORKSPACE_ID,
        stepId,
        null,
        BatchTraceOperationType.CREATE_PRODUCTION_BATCH,
        List.of(input),
        List.of(output),
        Instant.parse(occurredAt),
        BatchTraceActorReference.system("test"),
        null
    );
  }

  private static BatchTraceSubject subject(
      BatchTraceSubjectType type,
      String id
  ) {
    return BatchTraceSubject.of(type, id);
  }

  private static BatchTraceStepSubject state(
      String stateId,
      BatchTraceSubject subject,
      String volume,
      String sugar
  ) {
    return state(
        BatchTraceStateId.of(UUID.fromString(stateId)),
        subject,
        volume,
        sugar
    );
  }

  private static BatchTraceStepSubject state(
      BatchTraceStateId stateId,
      BatchTraceSubject subject,
      String volume,
      String sugar
  ) {
    return new BatchTraceStepSubject(
        stateId,
        subject,
        new BatchTraceMaterialSnapshot(
            VolumeValue.ofLiters(volume),
            ChemicalProfileAxes.of(
                new AxisValue(
                    ChemicalAxis.SUGAR,
                    new BigDecimal(sugar),
                    MeasurementValueKind.EXACT,
                    null,
                    null,
                    null,
                    null
                ),
                null,
                null,
                null
            ),
            null
        )
    );
  }

  private static BigDecimal sugarValue(BatchTraceNode node) {
    return node.snapshot().profile().valueOf(ChemicalAxis.SUGAR).orElseThrow().value();
  }
}
