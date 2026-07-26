package org.craft.mcs.cap.core.traceability.domain.graph;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceActorReference;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceMaterialSnapshot;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepType;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepId;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValueKind;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.testing.BatchTraceSubjects;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.testing.BatchTraceSubjects.id;


class BatchTraceabilityGraphProjectorTest {

  private static final WorkspaceId WORKSPACE_ID = WorkspaceId.newId();

  @Test
  void buildsSourceTreeFromSeveralImmutableStepsWithSameInputSubject() {
    final var feedstockSubject = subject(
        BatchTraceSubjectType.FEEDSTOCK_BATCH,
        "feedstock-1"
    );
    final var firstFeedstockState = state(feedstockSubject);
    final var secondFeedstockState = state(feedstockSubject);
    final var firstProduction = state(subject(
        BatchTraceSubjectType.PRODUCTION_BATCH,
        "production-1"
    ));
    final var secondProduction = state(subject(
        BatchTraceSubjectType.PRODUCTION_BATCH,
        "production-2"
    ));
    final var firstStep = step(firstFeedstockState, firstProduction, "2026-05-18T00:00:00Z");
    final var secondStep = step(secondFeedstockState, secondProduction, "2026-05-18T01:00:00Z");

    final var graph = BatchTraceabilityGraphProjector.sourceTreeFrom(List.of(firstStep, secondStep));

    assertThat(graph.nodes())
        .extracting(BatchTraceNode::subject)
        .containsExactly(
            firstFeedstockState.subject(),
            firstProduction.subject(),
            secondFeedstockState.subject(),
            secondProduction.subject()
        );
    assertThat(graph.edges()).hasSize(2);
    assertThat(graph.edges())
        .extracting(edge -> edge.inputs().getFirst().materialStateId())
        .containsExactly(
            firstFeedstockState.materialStateId(),
            secondFeedstockState.materialStateId()
        );
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
    final var feedstock = state(subject(
        BatchTraceSubjectType.FEEDSTOCK_BATCH,
        "feedstock-1"
    ));
    final var laterStep = step(
        BatchTraceStepId.of(UUID.fromString("00000000-0000-0000-0000-000000000001")),
        feedstock,
        state(subject(BatchTraceSubjectType.PRODUCTION_BATCH, "production-later")),
        "2026-05-18T02:00:00Z"
    );
    final var secondSameTimeStep = step(
        BatchTraceStepId.of(UUID.fromString("00000000-0000-0000-0000-000000000003")),
        feedstock,
        state(subject(BatchTraceSubjectType.PRODUCTION_BATCH, "production-second")),
        "2026-05-18T01:00:00Z"
    );
    final var firstSameTimeStep = step(
        BatchTraceStepId.of(UUID.fromString("00000000-0000-0000-0000-000000000002")),
        feedstock,
        state(subject(BatchTraceSubjectType.PRODUCTION_BATCH, "production-first")),
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
        state(subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-1")),
        initial,
        "2026-05-18T01:00:00Z"
    );
    final var secondStep = step(
        state(subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-2")),
        conflicting,
        "2026-05-18T02:00:00Z"
    );
    final var steps = List.of(firstStep, secondStep);

    assertThatThrownBy(() -> BatchTraceabilityGraphProjector.sourceTreeFrom(steps))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("material state id cannot identify different output subjects or state snapshots");
  }

  @Test
  void preservesStepParticipationSnapshotWithoutRedefiningMaterialStateNode() {
    final var feedstock = subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-1");
    final var production = subject(BatchTraceSubjectType.PRODUCTION_BATCH, "production-1");
    final var feedstockStateId = MaterialStateId.newId();
    final var stateDefinition = state(feedstockStateId, feedstock, "530", "1.054");
    final var consumedParticipation = state(feedstockStateId, feedstock, "50", "1.054");
    final var creation = step(
        state(subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "orchard-source")),
        stateDefinition,
        "2026-05-18T01:00:00Z"
    );
    final var productionCreation = step(
        consumedParticipation,
        state(production),
        "2026-05-18T02:00:00Z"
    );

    final var graph = BatchTraceabilityGraphProjector.sourceTreeFrom(List.of(
        productionCreation,
        creation
    ));

    assertThat(graph.nodes())
        .filteredOn(node -> node.materialStateId().equals(feedstockStateId))
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
    final var input = state(subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-1"));
    final var output = state(subject(BatchTraceSubjectType.PRODUCTION_BATCH, "production-1"));
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

  @Test
  void createsEmptyGraph() {
    assertThat(BatchTraceabilityGraph.empty())
        .returns(List.of(), BatchTraceabilityGraph::nodes)
        .returns(List.of(), BatchTraceabilityGraph::edges);
  }

  @Test
  void rejectsOneMaterialStateIdentityReferencingDifferentBusinessSubjects() {
    final var sharedStateId = MaterialStateId.newId();
    final var firstInput = BatchTraceStepSubject.reference(
        sharedStateId,
        subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-1")
    );
    final var secondInput = BatchTraceStepSubject.reference(
        sharedStateId,
        subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-2")
    );
    final var firstStep = step(
        firstInput,
        state(subject(BatchTraceSubjectType.PRODUCTION_BATCH, "production-1")),
        "2026-05-18T01:00:00Z"
    );
    final var secondStep = step(
        secondInput,
        state(subject(BatchTraceSubjectType.PRODUCTION_BATCH, "production-2")),
        "2026-05-18T02:00:00Z"
    );
    final var steps = List.of(firstStep, secondStep);

    assertThatThrownBy(() -> BatchTraceabilityGraphProjector.sourceTreeFrom(steps))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("material state id cannot reference different business subjects");
  }

  @Test
  void rejectsDuplicateGraphNodesForOneMaterialState() {
    final var state = state(subject(BatchTraceSubjectType.PRODUCTION_BATCH, "production-1"));
    final var node = BatchTraceNode.from(state);
    final var nodes = List.of(node, node);

    assertThatThrownBy(() -> new BatchTraceabilityGraph(nodes, List.of()))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("batch traceability graph cannot contain duplicate state nodes");
  }

  @Test
  void rejectsGraphEdgeThatAssociatesStateWithDifferentSubject() {
    final var stateId = MaterialStateId.newId();
    final var graphState = state(
        stateId,
        subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-1"),
        "100",
        "1.052"
    );
    final var conflictingReference = BatchTraceStepSubject.reference(
        stateId,
        subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-2")
    );
    final var output = state(subject(BatchTraceSubjectType.PRODUCTION_BATCH, "production-1"));
    final var edge = new BatchTraceEdge(
        BatchTraceStepId.newId(),
        BatchTraceStepType.PRODUCTION_BATCH_CREATED,
        Instant.parse("2026-05-18T00:00:00Z"),
        List.of(conflictingReference),
        List.of(output)
    );
    final var nodes = List.of(BatchTraceNode.from(graphState), BatchTraceNode.from(output));

    assertThatThrownBy(() -> new BatchTraceabilityGraph(nodes, List.of(edge)))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("batch trace edge state must reference its graph-node subject");
  }

  @Test
  void rejectsGraphOutputThatDiffersFromImmutableNodeDefinition() {
    final var outputSubject = subject(BatchTraceSubjectType.PRODUCTION_BATCH, "production-1");
    final var stateId = MaterialStateId.newId();
    final var nodeDefinition = state(stateId, outputSubject, "100", "1.052");
    final var conflictingOutput = state(stateId, outputSubject, "92", "1.052");
    final var input = state(subject(BatchTraceSubjectType.FEEDSTOCK_BATCH, "feedstock-1"));
    final var edge = new BatchTraceEdge(
        BatchTraceStepId.newId(),
        BatchTraceStepType.PRODUCTION_BATCH_CREATED,
        Instant.parse("2026-05-18T00:00:00Z"),
        List.of(input),
        List.of(conflictingOutput)
    );
    final var nodes = List.of(BatchTraceNode.from(input), BatchTraceNode.from(nodeDefinition));

    assertThatThrownBy(() -> new BatchTraceabilityGraph(nodes, List.of(edge)))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("batch trace output state must match its immutable graph node");
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
    return BatchTraceSubjects.subject(type, fixtureReference);
  }

  private static BatchTraceStepSubject state(BatchTraceSubject subject) {
    return BatchTraceStepSubject.reference(MaterialStateId.newId(), subject);
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
