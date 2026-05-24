package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.mapper;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.craft.mcs.cap.core.batchtraceability.domain.graph.BatchTraceabilityGraph;
import org.craft.mcs.cap.core.batchtraceability.domain.graph.BatchTraceEdge;
import org.craft.mcs.cap.core.batchtraceability.domain.graph.BatchTraceNode;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceMaterialSnapshot;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.BatchTraceAxisValueResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.BatchTraceEdgeResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.BatchTraceMaterialSnapshotResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.BatchTraceNodeResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.BatchTraceStepSubjectResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.BatchTraceSubjectResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.BatchTraceabilityGraphResponse;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public final class BatchTraceabilityRestMapper {

  private BatchTraceabilityRestMapper() {
  }

  public static @NonNull BatchTraceabilityGraphResponse toResponse(@NonNull BatchTraceabilityGraph graph) {
    requireNonNull(graph, "batch traceability graph cannot be null");

    return new BatchTraceabilityGraphResponse(
        graph.nodes().stream()
            .map(BatchTraceabilityRestMapper::toResponse)
            .toList(),
        graph.edges().stream()
            .map(BatchTraceabilityRestMapper::toResponse)
            .toList()
    );
  }

  private static @NonNull BatchTraceNodeResponse toResponse(@NonNull BatchTraceNode node) {
    return new BatchTraceNodeResponse(
        toResponse(node.subject()),
        toResponse(node.snapshot())
    );
  }

  private static @NonNull BatchTraceEdgeResponse toResponse(@NonNull BatchTraceEdge edge) {
    return new BatchTraceEdgeResponse(
        edge.stepId().value(),
        edge.operationType(),
        edge.inputs().stream()
            .map(BatchTraceabilityRestMapper::toResponse)
            .toList(),
        edge.outputs().stream()
            .map(BatchTraceabilityRestMapper::toResponse)
            .toList()
    );
  }

  private static @NonNull BatchTraceStepSubjectResponse toResponse(@NonNull BatchTraceStepSubject subject) {
    return new BatchTraceStepSubjectResponse(
        toResponse(subject.subject()),
        toResponse(subject.snapshot())
    );
  }

  private static @NonNull BatchTraceSubjectResponse toResponse(@NonNull BatchTraceSubject subject) {
    return new BatchTraceSubjectResponse(
        subject.type(),
        subject.id(),
        subject.displayNameSnapshot()
    );
  }

  private static @Nullable BatchTraceMaterialSnapshotResponse toResponse(
      @Nullable BatchTraceMaterialSnapshot snapshot
  ) {
    if (snapshot == null) {
      return null;
    }

    return new BatchTraceMaterialSnapshotResponse(
        snapshot.volume() == null ? null : snapshot.volume().liters(), // NOSONAR
        axes(snapshot.profile()),
        snapshot.confidence()
    );
  }

  private static @NonNull List<@NonNull BatchTraceAxisValueResponse> axes(
      @Nullable ChemicalProfileAxes profile
  ) {
    if (profile == null) {
      return List.of();
    }

    return Arrays.stream(ChemicalAxis.values())
        .map(axis -> profile.valueOf(axis).orElse(null))
        .filter(Objects::nonNull)
        .map(BatchTraceabilityRestMapper::toResponse)
        .toList();
  }

  private static @NonNull BatchTraceAxisValueResponse toResponse(@NonNull AxisValue axisValue) {
    return new BatchTraceAxisValueResponse(
        axisValue.axis(),
        axisValue.value(),
        axisValue.valueKind(),
        axisValue.min(),
        axisValue.max(),
        axisValue.standardDeviation(),
        axisValue.sampleSize()
    );
  }
}
