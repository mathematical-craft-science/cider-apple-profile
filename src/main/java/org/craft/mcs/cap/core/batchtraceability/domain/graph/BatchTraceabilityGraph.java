package org.craft.mcs.cap.core.batchtraceability.domain.graph;

import java.util.List;
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
  }

  public static @NonNull BatchTraceabilityGraph empty() {
    return new BatchTraceabilityGraph(List.of(), List.of());
  }
}
