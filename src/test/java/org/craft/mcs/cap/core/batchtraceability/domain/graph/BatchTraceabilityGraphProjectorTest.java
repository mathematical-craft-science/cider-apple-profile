package org.craft.mcs.cap.core.batchtraceability.domain.graph;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceId;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceOperationType;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepId;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class BatchTraceabilityGraphProjectorTest {

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
    final var traceId = BatchTraceId.newId();
    final var firstStep = step(traceId, feedstock, firstProduction, "2026-05-18T00:00:00Z");
    final var secondStep = step(traceId, feedstock, secondProduction, "2026-05-18T01:00:00Z");

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
        BatchTraceId.newId(),
        null,
        BatchTraceOperationType.SPLIT,
        List.of(sourceProduction),
        List.of(firstSplitPart, secondSplitPart),
        Instant.parse("2026-05-18T02:00:00Z"),
        null,
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
    final var traceId = BatchTraceId.newId();
    final var laterStep = step(
        traceId,
        BatchTraceStepId.of(UUID.fromString("00000000-0000-0000-0000-000000000001")),
        feedstock,
        BatchTraceStepSubject.of(subject(BatchTraceSubjectType.PRODUCTION_BATCH, "production-later")),
        "2026-05-18T02:00:00Z"
    );
    final var secondSameTimeStep = step(
        traceId,
        BatchTraceStepId.of(UUID.fromString("00000000-0000-0000-0000-000000000003")),
        feedstock,
        BatchTraceStepSubject.of(subject(BatchTraceSubjectType.PRODUCTION_BATCH, "production-second")),
        "2026-05-18T01:00:00Z"
    );
    final var firstSameTimeStep = step(
        traceId,
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

  private static BatchTraceStep step(
      BatchTraceId traceId,
      BatchTraceStepSubject input,
      BatchTraceStepSubject output,
      String occurredAt
  ) {
    return step(traceId, BatchTraceStepId.newId(), input, output, occurredAt);
  }

  private static BatchTraceStep step(
      BatchTraceId traceId,
      BatchTraceStepId stepId,
      BatchTraceStepSubject input,
      BatchTraceStepSubject output,
      String occurredAt
  ) {
    return new BatchTraceStep(
        traceId,
        stepId,
        null,
        BatchTraceOperationType.CREATE_PRODUCTION_BATCH,
        List.of(input),
        List.of(output),
        Instant.parse(occurredAt),
        null,
        null
    );
  }

  private static BatchTraceSubject subject(BatchTraceSubjectType type, String id) {
    return BatchTraceSubject.of(type, id);
  }
}
