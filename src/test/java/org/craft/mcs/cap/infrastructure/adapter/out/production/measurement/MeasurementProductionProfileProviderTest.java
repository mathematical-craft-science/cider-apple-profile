package org.craft.mcs.cap.infrastructure.adapter.out.production.measurement;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.craft.mcs.cap.core.measurement.application.model.CurrentMeasurementProfileView;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementEvidenceView;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementFreshnessStatus;
import org.craft.mcs.cap.core.measurement.application.port.in.ProjectCurrentMeasurementProfileUseCase;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCharacteristic;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementMethod;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementProvenance;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSourceName;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSourceReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSourceType;
import org.craft.mcs.cap.core.production.application.model.ProductionMeasurementEvidenceReference;
import org.craft.mcs.cap.core.production.application.model.ProductionMeasurementEvidenceView;
import org.craft.mcs.cap.core.production.application.model.ProductionMeasurementProfileView;
import org.craft.mcs.cap.core.production.application.model.ProductionMeasurementProvenanceView;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class MeasurementProductionProfileProviderTest {

  @Test
  void translatesGenericMeasurementProjectionToProductionOwnedModel() {
    final var batchId = ProductionBatchId.newId();
    final var evidence = new MeasurementEvidenceView(
        MeasurementCharacteristic.SPECIFIC_GRAVITY,
        MeasurementSetId.newId(),
        MeasurementRecordId.newId(),
        Instant.parse("2026-07-24T01:00:00Z"),
        new MeasurementProvenance(
            MeasurementSourceType.INSTRUMENT,
            MeasurementSourceName.of("Hydrometer H-01"),
            MeasurementMethod.of("temperature compensated"),
            MeasurementSourceReference.of("record-42"),
            ConfidenceLevel.HIGH
        ),
        MeasurementFreshnessStatus.CURRENT
    );
    final var measurementProfile = new CurrentMeasurementProfileView(
        ChemicalProfile.builder()
            .sugar(PointCharacteristicValue.of(SugarConcentration.ofSpecificGravity(
                SpecificGravityValue.of("1.035")
            )))
            .build(),
        List.of(evidence)
    );
    final var projector = mock(ProjectCurrentMeasurementProfileUseCase.class);
    when(projector.project(any())).thenReturn(Optional.of(measurementProfile));

    final var result = new MeasurementProductionProfileProvider(projector)
        .findCurrent(batchId)
        .orElseThrow();

    assertThat(result)
        .returns(measurementProfile.chemicalProfile(), ProductionMeasurementProfileView::chemicalProfile)
        .satisfies(profile -> assertThat(profile.evidence())
            .singleElement()
            .returns(
                ProductionMeasurementEvidenceView.Characteristic.SUGAR,
                ProductionMeasurementEvidenceView::characteristic
            )
            .satisfies(productionEvidence -> assertThat(productionEvidence.evidenceReference())
                .returns(evidence.measurementSetId().value(), ProductionMeasurementEvidenceReference::measurementSetId)
                .returns(evidence.recordId().value(), ProductionMeasurementEvidenceReference::recordId))
            .satisfies(productionEvidence -> assertThat(productionEvidence.provenance())
                .returns(
                    ProductionMeasurementProvenanceView.SourceType.INSTRUMENT,
                    ProductionMeasurementProvenanceView::sourceType
                )
                .returns("Hydrometer H-01", ProductionMeasurementProvenanceView::sourceName)
                .returns("temperature compensated", ProductionMeasurementProvenanceView::method)
                .returns("record-42", ProductionMeasurementProvenanceView::reference)
                .returns(ConfidenceLevel.HIGH, ProductionMeasurementProvenanceView::confidence))
            .returns(
                ProductionMeasurementEvidenceView.FreshnessStatus.CURRENT,
                ProductionMeasurementEvidenceView::freshnessStatus
            ));
  }
}
