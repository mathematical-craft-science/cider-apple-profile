package org.craft.mcs.cap.core.batchtraceability.domain.graph;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStateId;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Builds read-model graphs from immutable batch trace steps.
 *
 * <p>The projector derives nodes and edges from append-only facts and keys nodes by historical
 * state identity. Several states can therefore refer to the same business batch without replacing earlier volume or
 * chemical-profile snapshots.
 */
public final class BatchTraceabilityGraphProjector {

  private BatchTraceabilityGraphProjector() {
  }

  public static @NonNull BatchTraceabilityGraph sourceTreeFrom(
      @NonNull List<@NonNull BatchTraceStep> steps
  ) {
    final var nodesByState = new LinkedHashMap<BatchTraceStateId, BatchTraceNode>();
    final var edges = requireNonNull(steps, "batch trace steps cannot be null").stream()
        .sorted(Comparator.comparing(BatchTraceStep::occurredAt)
            .thenComparing(batchTraceStep -> batchTraceStep.stepId().value()))
        .map(step -> {
          registerNodes(nodesByState, step.inputs());
          registerNodes(nodesByState, step.outputs());
          return new BatchTraceEdge(
              step.stepId(),
              step.operationType(),
              step.occurredAt(),
              step.inputs(),
              step.outputs()
          );
        })
        .toList();

    return new BatchTraceabilityGraph(List.copyOf(nodesByState.values()), edges);
  }

  private static void registerNodes(
      @NonNull LinkedHashMap<BatchTraceStateId, BatchTraceNode> nodesByState,
      @NonNull List<@NonNull BatchTraceStepSubject> stepSubjects
  ) {
    for (final var stepSubject : stepSubjects) {
      final var node = BatchTraceNode.from(stepSubject);
      final var existingNode = nodesByState.putIfAbsent(stepSubject.traceStateId(), node);
      if (existingNode != null && !existingNode.equals(node)) {
        throw new DomainRuleViolationException(
            "batch trace state id cannot identify different subjects or snapshots"
        );
      }
    }
  }
}
