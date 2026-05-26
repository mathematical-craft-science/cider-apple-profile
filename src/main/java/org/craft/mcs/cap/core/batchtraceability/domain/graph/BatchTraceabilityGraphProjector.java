package org.craft.mcs.cap.core.batchtraceability.domain.graph;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Builds read-model graphs from immutable batch trace steps.
 *
 * <p>The projector intentionally derives nodes and edges from a collection of append-only facts
 * instead of updating an older step. This supports the normal production case where the same
 * feedstock batch is consumed by several independent production batches at different times.
 */
public final class BatchTraceabilityGraphProjector {

  private BatchTraceabilityGraphProjector() {
  }

  public static @NonNull BatchTraceabilityGraph sourceTreeFrom(
      @NonNull List<@NonNull BatchTraceStep> steps
  ) {
    final var nodesBySubject = new LinkedHashMap<BatchTraceSubject, BatchTraceNode>();
    final var edges = requireNonNull(steps, "batch trace steps cannot be null").stream()
        .sorted(Comparator.comparing(BatchTraceStep::occurredAt)
            .thenComparing(batchTraceStep -> batchTraceStep.stepId().value()))
        .map(step -> {
          registerNodes(nodesBySubject, step.inputs());
          registerNodes(nodesBySubject, step.outputs());
          return new BatchTraceEdge(
              step.stepId(),
              step.operationType(),
              step.occurredAt(),
              step.inputs(),
              step.outputs()
          );
        })
        .toList();

    return new BatchTraceabilityGraph(List.copyOf(nodesBySubject.values()), edges);
  }

  private static void registerNodes(
      @NonNull LinkedHashMap<BatchTraceSubject, BatchTraceNode> nodesBySubject,
      @NonNull List<@NonNull BatchTraceStepSubject> stepSubjects
  ) {
    for (final var stepSubject : stepSubjects) {
      nodesBySubject.putIfAbsent(
          stepSubject.subject(),
          new BatchTraceNode(stepSubject.subject(), stepSubject.snapshot())
      );
    }
  }
}
