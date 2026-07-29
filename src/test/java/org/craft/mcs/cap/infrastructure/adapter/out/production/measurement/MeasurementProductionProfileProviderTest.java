package org.craft.mcs.cap.infrastructure.adapter.out.production.measurement;

import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.core.measurement.application.model.CurrentMaterialCharacteristicView;
import org.craft.mcs.cap.core.measurement.application.model.CurrentMaterialStateView;
import org.craft.mcs.cap.core.measurement.application.port.in.ProjectCurrentMaterialStateUseCase;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementProvenance;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.core.production.application.model.ProductionMeasuredProfileView;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.shared.application.measurement.MeasurementFreshnessStatus;
import org.craft.mcs.cap.shared.application.measurement.MeasurementValueOrigin;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementSourceType;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
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
    final var characteristic = new CurrentMaterialCharacteristicView(
        MeasurementCharacteristic.SPECIFIC_GRAVITY,
        PointCharacteristicValue.of(SpecificGravityValue.of("1.035")),
        MeasurementValueOrigin.MEASURED,
        materialStateId,
        MeasurementSetId.of(newUuid()),
        MeasurementRecordId.of(newUuid()),
        Instant.parse("2026-07-24T01:00:00Z"),
        new MeasurementProvenance(
            MeasurementSourceType.INSTRUMENT,
            null,
            null,
            null,
            ConfidenceLevel.HIGH
        ),
        MeasurementFreshnessStatus.CURRENT
    );
    final var measurementProfile = new CurrentMaterialStateView(
        new MeasurementSubjectReference(MeasurementSubjectType.PRODUCTION_BATCH, batchId.value()),
        materialStateId,
        List.of(characteristic)
    );
    final var projector = mock(ProjectCurrentMaterialStateUseCase.class);
    when(projector.project(any())).thenReturn(measurementProfile);

    final var result = new MeasurementProductionProfileProvider(projector)
        .findCurrent(batchId)
        .orElseThrow();

    assertThat(result)
        .satisfies(profile -> assertThat(profile.specificGravity())
            .isNotNull()
            .returns(
                PointCharacteristicValue.of(SpecificGravityValue.of("1.035")),
                value -> value.value()
            )
            .returns(characteristic.evidence(), value -> value.evidence()));
  }
}
