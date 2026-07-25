package org.craft.mcs.cap.infrastructure.adapter.in.rest.batchtraceability.mapper;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.craft.mcs.cap.core.batchtraceability.domain.graph.BatchTraceabilityGraph;
import org.craft.mcs.cap.core.batchtraceability.domain.graph.BatchTraceEdge;
import org.craft.mcs.cap.core.batchtraceability.domain.graph.BatchTraceNode;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceMaterialSnapshot;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.batchtraceability.dto.BatchTraceAxisValueResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.batchtraceability.dto.BatchTraceEdgeResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.batchtraceability.dto.BatchTraceMaterialSnapshotResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.batchtraceability.dto.BatchTraceNodeResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.batchtraceability.dto.BatchTraceStepSubjectResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.batchtraceability.dto.BatchTraceSubjectResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.batchtraceability.dto.BatchTraceabilityGraphResponse;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
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
        node.traceStateId().value(),
        toResponse(node.subject()),
        toResponse(node.snapshot())
    );
  }

  private static @NonNull BatchTraceEdgeResponse toResponse(@NonNull BatchTraceEdge edge) {
    return new BatchTraceEdgeResponse(
        edge.stepId().value(),
        edge.stepType(),
        edge.occurredAt(),
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
        subject.traceStateId().value(),
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
        volume(snapshot),
        axes(snapshot.profile()),
        snapshot.confidence()
    );
  }

  private static @Nullable DecimalQuantityResponse volume(@NonNull BatchTraceMaterialSnapshot snapshot) {
    final var volume = snapshot.volume();
    return volume == null ? null : new DecimalQuantityResponse(volume.liters(), CanonicalUnitLabels.VOLUME);
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
    final var unit = CanonicalUnitLabels.forAxis(axisValue.axis());
    return new BatchTraceAxisValueResponse(
        axisValue.axis(),
        new DecimalQuantityResponse(axisValue.value(), unit),
        axisValue.valueKind(),
        quantity(axisValue.min(), unit),
        quantity(axisValue.max(), unit)
    );
  }

  private static @Nullable DecimalQuantityResponse quantity(
      @Nullable BigDecimal value,
      @NonNull String unit
  ) {
    return value == null ? null : new DecimalQuantityResponse(value, unit);
  }
}
