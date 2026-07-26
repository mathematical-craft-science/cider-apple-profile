package org.craft.mcs.cap.core.traceability.domain.graph;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Builds read-model graphs from immutable batch trace steps.
 *
 * <p>The projector derives nodes and edges from append-only facts and keys nodes by historical
 * state identity. Several states can therefore refer to the same business batch without replacing
 * earlier volume or chemical-profile snapshots.</p>
 */
public final class BatchTraceabilityGraphProjector {

  private BatchTraceabilityGraphProjector() {
  }

  public static @NonNull BatchTraceabilityGraph sourceTreeFrom(
      @NonNull List<@NonNull BatchTraceStep> steps
  ) {
    final var orderedSteps = requireNonNull(steps, "batch trace steps cannot be null").stream()
        .sorted(Comparator.comparing(BatchTraceStep::occurredAt)
            .thenComparing(batchTraceStep -> batchTraceStep.stepId().value()))
        .toList();
    final var stateDefinitions = new LinkedHashMap<MaterialStateId, BatchTraceNode>();
    orderedSteps.forEach(step -> registerStateDefinitions(stateDefinitions, step.outputs()));
    final var nodesByState = new LinkedHashMap<MaterialStateId, BatchTraceNode>();
    orderedSteps.forEach(step -> {
      registerGraphNodes(nodesByState, stateDefinitions, step.inputs());
      registerGraphNodes(nodesByState, stateDefinitions, step.outputs());
    });
    final var edges = orderedSteps.stream()
        .map(step -> new BatchTraceEdge(
            step.stepId(),
            step.stepType(),
            step.occurredAt(),
            step.inputs(),
            step.outputs()
        ))
        .toList();

    return new BatchTraceabilityGraph(List.copyOf(nodesByState.values()), edges);
  }

  private static void registerStateDefinitions(
      @NonNull LinkedHashMap<MaterialStateId, BatchTraceNode> nodesByState,
      @NonNull List<@NonNull BatchTraceStepSubject> stepSubjects
  ) {
    for (final var stepSubject : stepSubjects) {
      final var node = BatchTraceNode.from(stepSubject);
      final var existingNode = nodesByState.putIfAbsent(stepSubject.materialStateId(), node);
      if (existingNode != null && !existingNode.equals(node)) {
        throw new DomainRuleViolationException(
            "material state id cannot identify different output subjects or state snapshots"
        );
      }
    }
  }

  private static void registerGraphNodes(
      @NonNull LinkedHashMap<MaterialStateId, BatchTraceNode> nodesByState,
      @NonNull LinkedHashMap<MaterialStateId, BatchTraceNode> stateDefinitions,
      @NonNull List<@NonNull BatchTraceStepSubject> stepSubjects
  ) {
    for (final var stepSubject : stepSubjects) {
      final var node = stateDefinitions.getOrDefault(
          stepSubject.materialStateId(),
          BatchTraceNode.from(stepSubject)
      );
      final var existingNode = nodesByState.putIfAbsent(
          stepSubject.materialStateId(),
          node
      );
      if (existingNode != null && !sameSubject(existingNode.subject(), stepSubject.subject())) {
        throw new DomainRuleViolationException(
            "material state id cannot reference different business subjects"
        );
      }
    }
  }

  private static boolean sameSubject(
      @NonNull BatchTraceSubject first,
      @NonNull BatchTraceSubject second
  ) {
    return first.type() == second.type() && first.id().equals(second.id());
  }
}
