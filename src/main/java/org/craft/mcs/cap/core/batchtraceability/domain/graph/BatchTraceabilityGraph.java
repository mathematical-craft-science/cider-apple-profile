package org.craft.mcs.cap.core.batchtraceability.domain.graph;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@ValueObject
public record BatchTraceabilityGraph(
    @NonNull List<@NonNull BatchTraceNode> nodes,
    @NonNull List<@NonNull BatchTraceEdge> edges
) {

  public BatchTraceabilityGraph {
    nodes = List.copyOf(requireNonNull(nodes, "batch traceability graph nodes cannot be null"));
    edges = List.copyOf(requireNonNull(edges, "batch traceability graph edges cannot be null"));
    validateStateReferences(nodes, edges);
  }

  public static @NonNull BatchTraceabilityGraph empty() {
    return new BatchTraceabilityGraph(List.of(), List.of());
  }

  private static void validateStateReferences(
      @NonNull List<@NonNull BatchTraceNode> nodes,
      @NonNull List<@NonNull BatchTraceEdge> edges
  ) {
    final var nodesByState = new LinkedHashMap<MaterialStateId, BatchTraceNode>();
    for (final var node : nodes) {
      if (nodesByState.putIfAbsent(node.traceStateId(), node) != null) {
        throw new DomainRuleViolationException("batch traceability graph cannot contain duplicate state nodes");
      }
    }

    for (final var edge : edges) {
      edge.inputs().forEach(subject -> requireStateReference(nodesByState, subject));
      edge.outputs().forEach(subject -> validateStateDefinition(nodesByState, subject));
    }
  }

  private static @NonNull BatchTraceNode requireStateReference(
      @NonNull Map<MaterialStateId, BatchTraceNode> nodesByState,
      @NonNull BatchTraceStepSubject subject
  ) {
    final var node = nodesByState.get(subject.traceStateId());
    if (node == null) {
      throw new DomainRuleViolationException("batch trace edge state must be present in graph nodes");
    }
    if (node.subject().type() != subject.subject().type()
        || !node.subject().id().equals(subject.subject().id())) {
      throw new DomainRuleViolationException("batch trace edge state must reference its graph-node subject");
    }
    return node;
  }

  private static void validateStateDefinition(
      @NonNull Map<MaterialStateId, BatchTraceNode> nodesByState,
      @NonNull BatchTraceStepSubject subject
  ) {
    final var node = requireStateReference(nodesByState, subject);
    if (!node.equals(BatchTraceNode.from(subject))) {
      throw new DomainRuleViolationException("batch trace output state must match its immutable graph node");
    }
  }
}
