package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.mapper;

import org.craft.mcs.cap.core.measurement.application.model.MeasurementFreshnessStatus;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCharacteristic;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSourceType;
import org.craft.mcs.cap.core.production.application.model.ProductionMeasurementEvidenceView;
import org.craft.mcs.cap.core.production.application.model.ProductionMeasurementEvidenceView.Characteristic;
import org.craft.mcs.cap.core.production.application.model.ProductionMeasurementEvidenceView.FreshnessStatus;
import org.craft.mcs.cap.core.production.application.model.ProductionMeasurementProvenanceView.SourceType;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementEvidenceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementProvenanceResponse;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Maps Production-owned measurement evidence to the shared measurement transport contract. */
final class ProductionMeasurementEvidenceRestMapper {

  private ProductionMeasurementEvidenceRestMapper() {
  }

  static @NonNull MeasurementEvidenceResponse toResponse(
      @NonNull ProductionMeasurementEvidenceView evidence
  ) {
    requireNonNull(evidence, "production measurement evidence cannot be null");
    final var evidenceReference = evidence.evidenceReference();
    final var provenance = evidence.provenance();
    return new MeasurementEvidenceResponse(
        characteristic(evidence.characteristic()),
        evidenceReference.measurementSetId(),
        evidenceReference.recordId(),
        evidence.measuredAt(),
        freshnessStatus(evidence.freshnessStatus()),
        new MeasurementProvenanceResponse(
            sourceType(provenance.sourceType()),
            provenance.sourceName(),
            provenance.method(),
            provenance.reference(),
            provenance.confidence()
        )
    );
  }

  private static @NonNull MeasurementCharacteristic characteristic(
      @NonNull Characteristic characteristic
  ) {
    return switch (characteristic) {
      case SUGAR -> MeasurementCharacteristic.SPECIFIC_GRAVITY;
      case PH -> MeasurementCharacteristic.PH;
      case TITRATABLE_ACIDITY -> MeasurementCharacteristic.TITRATABLE_ACIDITY;
      case TANNIN -> MeasurementCharacteristic.TANNIN;
      case YAN -> MeasurementCharacteristic.YAN;
    };
  }

  private static @NonNull MeasurementSourceType sourceType(@NonNull SourceType sourceType) {
    return switch (sourceType) {
      case USER -> MeasurementSourceType.USER;
      case INSTRUMENT -> MeasurementSourceType.INSTRUMENT;
      case LAB -> MeasurementSourceType.LAB;
      case IMPORT -> MeasurementSourceType.IMPORT;
      case CURATOR -> MeasurementSourceType.CURATOR;
      case INSTITUTE -> MeasurementSourceType.INSTITUTE;
      case DERIVED -> MeasurementSourceType.DERIVED;
    };
  }

  private static @NonNull MeasurementFreshnessStatus freshnessStatus(
      @NonNull FreshnessStatus freshnessStatus
  ) {
    return switch (freshnessStatus) {
      case CURRENT -> MeasurementFreshnessStatus.CURRENT;
      case STALE -> MeasurementFreshnessStatus.STALE;
    };
  }
}
