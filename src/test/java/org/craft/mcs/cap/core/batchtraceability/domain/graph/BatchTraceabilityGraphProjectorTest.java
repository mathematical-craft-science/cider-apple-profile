package org.craft.mcs.cap.core.batchtraceability.domain.graph;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceActorReference;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceMaterialSnapshot;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepType;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepId;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValueKind;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.testing.BatchTraceSubjects.id;


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
        .containsExactly(id("production-1"), id("production-2"));
    assertThat(graph.edges())
        .extracting(BatchTraceEdge::occurredAt)
        .containsExactly(
            Instant.parse("2026-05-18T00:00:00Z"),
            Instant.parse("2026-05-18T01:00:00Z")
        );
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
        .containsExactly(id("production-first"), id("production-second"), id("production-later"));
  }

  @Test
  void rejectsDifferentOutputSnapshotsForTheSameTraceStateId() {
    final var production = subject(BatchTraceSubjectType.PRODUCTION_BATCH, "production-1");
    final var stateId = MaterialStateId.newId();
    final var initial = state(stateId, production, "100", "1.060");
    final var conflicting = state(stateId, production, "92", "1.060");
    final var firstStep = step(
        BatchTraceStepSubject.of(subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-1")),
        initial,
        "2026-05-18T01:00:00Z"
    );
    final var secondStep = step(
        BatchTraceStepSubject.of(subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-2")),
        conflicting,
        "2026-05-18T02:00:00Z"
    );
    final var steps = List.of(firstStep, secondStep);

    assertThatThrownBy(() -> BatchTraceabilityGraphProjector.sourceTreeFrom(steps))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("batch trace state id cannot identify different output subjects or state snapshots");
  }

  @Test
  void preservesStepParticipationSnapshotWithoutRedefiningMaterialStateNode() {
    final var feedstock = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-1");
    final var production = subject(BatchTraceSubjectType.PRODUCTION_BATCH, "production-1");
    final var feedstockStateId = MaterialStateId.newId();
    final var stateDefinition = state(feedstockStateId, feedstock, "530", "1.054");
    final var consumedParticipation = state(feedstockStateId, feedstock, "50", "1.054");
    final var creation = step(
        BatchTraceStepSubject.of(subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "orchard-source")),
        stateDefinition,
        "2026-05-18T01:00:00Z"
    );
    final var productionCreation = step(
        consumedParticipation,
        BatchTraceStepSubject.of(production),
        "2026-05-18T02:00:00Z"
    );

    final var graph = BatchTraceabilityGraphProjector.sourceTreeFrom(List.of(
        productionCreation,
        creation
    ));

    assertThat(graph.nodes())
        .filteredOn(node -> node.traceStateId().equals(feedstockStateId))
        .singleElement()
        .satisfies(node -> assertThat(node.snapshot())
            .isNotNull()
            .satisfies(snapshot -> assertThat(snapshot.volume())
                .isNotNull()
                .satisfies(volume -> assertThat(volume.liters()).isEqualByComparingTo("530"))));
    assertThat(graph.edges()).last()
        .satisfies(edge -> assertThat(edge.inputs()).singleElement()
            .satisfies(input -> assertThat(input.snapshot())
                .isNotNull()
                .satisfies(snapshot -> assertThat(snapshot.volume())
                    .isNotNull()
                    .satisfies(volume -> assertThat(volume.liters()).isEqualByComparingTo("50")))));
  }

  @Test
  void rejectsEdgeStateThatIsMissingFromGraphNodes() {
    final var input = BatchTraceStepSubject.of(subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-1"));
    final var output = BatchTraceStepSubject.of(subject(BatchTraceSubjectType.PRODUCTION_BATCH, "production-1"));
    final var edge = new BatchTraceEdge(
        BatchTraceStepId.newId(),
        BatchTraceStepType.PRODUCTION_BATCH_CREATED,
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
      BatchTraceStepId stepId,
      BatchTraceStepSubject input,
      BatchTraceStepSubject output,
      String occurredAt
  ) {
    return new BatchTraceStep(
        WORKSPACE_ID,
        stepId,
        null,
        BatchTraceStepType.PRODUCTION_BATCH_CREATED,
        List.of(input),
        List.of(output),
        Instant.parse(occurredAt),
        BatchTraceActorReference.system("test"),
        null
    );
  }

  private static BatchTraceSubject subject(
      BatchTraceSubjectType type,
      String fixtureReference
  ) {
    return org.craft.mcs.cap.shared.testing.BatchTraceSubjects.subject(type, fixtureReference);
  }

  private static BatchTraceStepSubject state(
      MaterialStateId stateId,
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
                    AxisValueKind.POINT,
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

}
