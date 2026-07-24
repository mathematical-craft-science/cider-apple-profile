package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement;

import org.craft.mcs.cap.core.measurement.application.model.MeasurementEvidenceView;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementEvidenceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementProvenanceResponse;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.shared.measurement.MeasurementProvenanceValueMapper.methodValue;
import static org.craft.mcs.cap.infrastructure.adapter.shared.measurement.MeasurementProvenanceValueMapper.referenceValue;
import static org.craft.mcs.cap.infrastructure.adapter.shared.measurement.MeasurementProvenanceValueMapper.sourceNameValue;


/** Maps selected current-profile evidence without exposing measurement persistence details. */
public final class MeasurementEvidenceRestMapper {

  private MeasurementEvidenceRestMapper() {
  }

  public static @NonNull MeasurementEvidenceResponse toResponse(
      @NonNull MeasurementEvidenceView evidence
  ) {
    requireNonNull(evidence, "measurement evidence cannot be null");
    final var provenance = evidence.provenance();
    return new MeasurementEvidenceResponse(
        evidence.characteristic(),
        evidence.measurementSetId().value(),
        evidence.recordId().value(),
        evidence.measuredAt(),
        evidence.freshnessStatus(),
        new MeasurementProvenanceResponse(
            provenance.sourceType(),
            sourceNameValue(provenance.sourceName()),
            methodValue(provenance.method()),
            referenceValue(provenance.reference()),
            provenance.confidence()
        )
    );
  }
}
