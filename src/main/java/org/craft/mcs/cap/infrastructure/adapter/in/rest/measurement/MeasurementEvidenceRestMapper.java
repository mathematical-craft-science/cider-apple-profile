package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement;

import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceView;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementEvidenceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementProvenanceResponse;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Maps selected current-profile evidence without exposing measurement persistence details. */
public final class MeasurementEvidenceRestMapper {

  private MeasurementEvidenceRestMapper() {
  }

  public static @NonNull MeasurementEvidenceResponse toResponse(
      @NonNull MeasurementEvidenceView evidence
  ) {
    requireNonNull(evidence, "measurement evidence cannot be null");
    final var provenance = evidence.provenance();
    final var evidenceReference = evidence.evidenceReference();
    return new MeasurementEvidenceResponse(
        evidence.characteristic(),
        evidence.origin(),
        evidence.materialStateId().value(),
        evidenceReference.measurementSetId(),
        evidenceReference.recordId(),
        evidence.measuredAt(),
        evidence.freshnessStatus(),
        new MeasurementProvenanceResponse(
            provenance.sourceType(),
            provenance.sourceName(),
            provenance.method(),
            provenance.reference(),
            provenance.confidence()
        )
    );
  }
}
