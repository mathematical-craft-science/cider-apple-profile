package org.craft.mcs.cap.infrastructure.adapter.out.production.measurement;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.craft.mcs.cap.core.measurement.application.compatibility.CurrentMeasurementProfileView;
import org.craft.mcs.cap.core.measurement.application.compatibility.ProjectCurrentMeasurementProfileUseCase;
import org.craft.mcs.cap.core.production.application.model.ProductionMeasuredProfileView;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceReference;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceValidity;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceView;
import org.craft.mcs.cap.shared.application.measurement.MeasurementFreshnessStatus;
import org.craft.mcs.cap.shared.application.measurement.MeasurementProvenanceView;
import org.craft.mcs.cap.shared.application.measurement.MeasurementValueOrigin;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementSourceType;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;


class MeasurementProductionProfileProviderTest {

  @Test
  void exposesPublishedMeasurementEvidenceWithoutContextSpecificTranslation() {
    final var batchId = ProductionBatchId.newId();
    final var materialStateId = MaterialStateId.newId();
    final var evidence = new MeasurementEvidenceView(
        MeasurementCharacteristic.SPECIFIC_GRAVITY,
        MeasurementValueOrigin.MEASURED,
        materialStateId,
        new MeasurementEvidenceReference(newUuid(), newUuid()),
        Instant.parse("2026-07-24T01:00:00Z"),
        new MeasurementProvenanceView(
            MeasurementSourceType.INSTRUMENT,
            "Hydrometer H-01",
            "temperature compensated",
            "record-42",
            ConfidenceLevel.HIGH
        ),
        MeasurementFreshnessStatus.CURRENT,
        MeasurementEvidenceValidity.VALID
    );
    final var measurementProfile = new CurrentMeasurementProfileView(
        materialStateId,
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
        .returns(measurementProfile.chemicalProfile(), ProductionMeasuredProfileView::chemicalProfile)
        .returns(List.of(evidence), ProductionMeasuredProfileView::evidence);
  }
}
