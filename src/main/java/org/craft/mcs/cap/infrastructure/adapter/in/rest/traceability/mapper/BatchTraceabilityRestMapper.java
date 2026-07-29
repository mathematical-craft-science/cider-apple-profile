package org.craft.mcs.cap.infrastructure.adapter.in.rest.traceability.mapper;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.craft.mcs.cap.core.traceability.domain.graph.BatchTraceabilityGraph;
import org.craft.mcs.cap.core.traceability.domain.graph.BatchTraceEdge;
import org.craft.mcs.cap.core.traceability.domain.graph.BatchTraceNode;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceCharacteristicSnapshot;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceMaterialCharacteristics;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceMaterialSnapshot;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.dto.CalculatedProfileCharacteristicValueResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.dto.ProfileCharacteristicValueResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.mapper.ProfileCharacteristicRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.traceability.dto.BatchTraceCharacteristicResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.traceability.dto.BatchTraceEdgeResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.traceability.dto.BatchTraceMaterialSnapshotResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.traceability.dto.BatchTraceNodeResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.traceability.dto.BatchTraceStepSubjectResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.traceability.dto.BatchTraceSubjectResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.traceability.dto.BatchTraceabilityGraphResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
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
        node.materialStateId().value(),
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
        subject.materialStateId().value(),
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
        characteristics(snapshot.characteristics()),
        snapshot.confidence()
    );
  }

  private static @Nullable DecimalQuantityResponse volume(@NonNull BatchTraceMaterialSnapshot snapshot) {
    final var volume = snapshot.volume();
    return volume == null ? null : new DecimalQuantityResponse(volume.liters(), CanonicalUnitLabels.VOLUME);
  }

  private static @NonNull List<@NonNull BatchTraceCharacteristicResponse> characteristics(
      @Nullable BatchTraceMaterialCharacteristics characteristics
  ) {
    if (characteristics == null) {
      return List.of();
    }

    return Stream.of(
            toResponse(MeasurementCharacteristic.SPECIFIC_GRAVITY, characteristics.specificGravity()),
            toResponse(MeasurementCharacteristic.TITRATABLE_ACIDITY, characteristics.titratableAcidity()),
            toResponse(MeasurementCharacteristic.TANNIN, characteristics.tannin()),
            toResponse(MeasurementCharacteristic.YAN, characteristics.yeastAssimilableNitrogen())
        )
        .filter(Objects::nonNull)
        .toList();
  }

  private static <T extends MeasuredValue> @Nullable BatchTraceCharacteristicResponse toResponse(
      @NonNull MeasurementCharacteristic characteristic,
      @Nullable BatchTraceCharacteristicSnapshot<T> snapshot
  ) {
    if (snapshot == null) {
      return null;
    }

    final var unit = CanonicalUnitLabels.forCharacteristic(characteristic);
    final ProfileCharacteristicValueResponse value = switch (snapshot) {
      case BatchTraceCharacteristicSnapshot.Direct<T>(var directValue) ->
          ProfileCharacteristicRestMapper.toResponse(
              directValue,
              MeasuredValue::asBigDecimal,
              unit
          );
      case BatchTraceCharacteristicSnapshot.Calculated<T>(var calculatedValue) ->
          new CalculatedProfileCharacteristicValueResponse(calculatedValue.asBigDecimal(), unit);
    };
    return new BatchTraceCharacteristicResponse(characteristic, value);
  }
}
