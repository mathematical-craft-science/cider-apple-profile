package org.craft.mcs.cap.infrastructure.adapter.in.rest.materialstate;

import org.craft.mcs.cap.core.measurement.application.model.CurrentMaterialStateView;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.materialstate.dto.CurrentMaterialCharacteristicResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.materialstate.dto.CurrentMaterialStateResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.MeasurementValueRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementProvenanceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementSubjectResponse;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.shared.measurement.MeasurementProvenanceValueMapper.methodValue;
import static org.craft.mcs.cap.infrastructure.adapter.shared.measurement.MeasurementProvenanceValueMapper.referenceValue;
import static org.craft.mcs.cap.infrastructure.adapter.shared.measurement.MeasurementProvenanceValueMapper.sourceNameValue;


/** Maps the authoritative state-aware Measurement projection to its REST contract. */
final class CurrentMaterialStateRestMapper {

  private CurrentMaterialStateRestMapper() {
  }

  static @NonNull CurrentMaterialStateResponse toResponse(@NonNull CurrentMaterialStateView state) {
    requireNonNull(state, "current material state cannot be null");
    return new CurrentMaterialStateResponse(
        new MeasurementSubjectResponse(state.subject().type(), state.subject().id()),
        state.currentMaterialStateId().value(),
        state.characteristics().stream()
            .map(characteristic -> {
              final var provenance = characteristic.provenance();
              return new CurrentMaterialCharacteristicResponse(
                  characteristic.characteristic(),
                  MeasurementValueRestMapper.toResponse(
                      characteristic.characteristic(),
                      characteristic.value()
                  ),
                  characteristic.origin(),
                  characteristic.evidenceMaterialStateId().value(),
                  characteristic.measurementSetId().value(),
                  characteristic.recordId().value(),
                  characteristic.measuredAt(),
                  new MeasurementProvenanceResponse(
                      provenance.sourceType(),
                      sourceNameValue(provenance.sourceName()),
                      methodValue(provenance.method()),
                      referenceValue(provenance.reference()),
                      provenance.confidence()
                  ),
                  characteristic.freshnessStatus(),
                  characteristic.validity()
              );
            })
            .toList()
    );
  }
}
