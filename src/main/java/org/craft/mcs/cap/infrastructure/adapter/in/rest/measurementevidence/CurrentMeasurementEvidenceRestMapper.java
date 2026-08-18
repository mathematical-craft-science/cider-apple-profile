package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurementevidence;

import org.craft.mcs.cap.core.measurement.application.model.CurrentMeasurementCharacteristicView;
import org.craft.mcs.cap.core.measurement.application.model.CurrentMeasurementEvidenceView;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.MeasurementValueRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementProvenanceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementSubjectResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurementevidence.dto.CurrentMeasurementCharacteristicResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurementevidence.dto.CurrentMeasurementEvidenceResponse;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.shared.measurement.MeasurementProvenanceValueMapper.methodValue;
import static org.craft.mcs.cap.infrastructure.adapter.shared.measurement.MeasurementProvenanceValueMapper.referenceValue;
import static org.craft.mcs.cap.infrastructure.adapter.shared.measurement.MeasurementProvenanceValueMapper.sourceNameValue;


/** Maps state-aware Measurement evidence to its canonical REST contract. */
public final class CurrentMeasurementEvidenceRestMapper {

  private CurrentMeasurementEvidenceRestMapper() {
  }

  public static @NonNull CurrentMeasurementEvidenceResponse toResponse(
      @NonNull CurrentMeasurementEvidenceView evidence
  ) {
    requireNonNull(evidence, "current measurement evidence cannot be null");
    return new CurrentMeasurementEvidenceResponse(
        new MeasurementSubjectResponse(evidence.subject().type(), evidence.subject().id()),
        evidence.currentMaterialStateId().value(),
        evidence.characteristics().stream()
            .map(CurrentMeasurementEvidenceRestMapper::toResponse)
            .toList()
    );
  }

  private static @NonNull CurrentMeasurementCharacteristicResponse toResponse(
      @NonNull CurrentMeasurementCharacteristicView characteristic
  ) {
    final var provenance = characteristic.provenance();
    return new CurrentMeasurementCharacteristicResponse(
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
        characteristic.freshnessStatus()
    );
  }
}
