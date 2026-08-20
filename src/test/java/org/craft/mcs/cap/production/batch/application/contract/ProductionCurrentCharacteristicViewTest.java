package org.craft.mcs.cap.production.batch.application.contract;

import java.time.Instant;
import org.craft.mcs.cap.core.production.batch.application.contract.ProductionCharacteristicOrigin;
import org.craft.mcs.cap.core.production.batch.application.contract.ProductionCurrentCharacteristicView;
import org.craft.mcs.cap.core.production.batch.application.contract.ProductionMeasuredProfileView;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceReference;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceView;
import org.craft.mcs.cap.shared.application.measurement.MeasurementFreshnessStatus;
import org.craft.mcs.cap.shared.application.measurement.MeasurementProvenanceView;
import org.craft.mcs.cap.shared.application.measurement.MeasurementValueOrigin;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementSourceType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;


class ProductionCurrentCharacteristicViewTest {

  private static final PointCharacteristicValue<SpecificGravityValue> VALUE =
      PointCharacteristicValue.of(SpecificGravityValue.of("1.035"));

  @Test
  void createsMeasurementBackedCharacteristic() {
    final var evidence = evidence();

    assertThat(ProductionCurrentCharacteristicView.fromMeasurement(VALUE, evidence))
        .returns(VALUE, ProductionCurrentCharacteristicView::value)
        .returns(ProductionCharacteristicOrigin.MEASUREMENT, ProductionCurrentCharacteristicView::origin)
        .returns(evidence, ProductionCurrentCharacteristicView::evidence);
  }

  @Test
  void createsInitialSnapshotCharacteristicWithoutMeasurementEvidence() {
    assertThat(ProductionCurrentCharacteristicView.fromInitialSnapshot(VALUE))
        .returns(VALUE, ProductionCurrentCharacteristicView::value)
        .returns(ProductionCharacteristicOrigin.INITIAL_SNAPSHOT, ProductionCurrentCharacteristicView::origin)
        .returns(null, ProductionCurrentCharacteristicView::evidence);
  }

  @Test
  void rejectsMeasuredOriginWithoutEvidence() {
    assertThatThrownBy(() -> new ProductionCurrentCharacteristicView<>(
        VALUE,
        ProductionCharacteristicOrigin.MEASUREMENT,
        null
    ))
        .isInstanceOf(ApplicationValidationException.class);
  }

  @Test
  void rejectsInitialOriginWithMeasurementEvidence() {
    assertThatThrownBy(() -> new ProductionCurrentCharacteristicView<>(
        VALUE,
        ProductionCharacteristicOrigin.INITIAL_SNAPSHOT,
        evidence()
    ))
        .isInstanceOf(ApplicationValidationException.class);
  }

  @Test
  void measuredProfileRejectsInitialSnapshotCharacteristics() {
    final var initial = ProductionCurrentCharacteristicView.fromInitialSnapshot(VALUE);

    assertThatThrownBy(() -> new ProductionMeasuredProfileView(
        initial,
        null,
        null,
        null,
        null
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessageContaining("measurement-backed characteristics");
  }

  private static MeasurementEvidenceView evidence() {
    return new MeasurementEvidenceView(
        MeasurementCharacteristic.SPECIFIC_GRAVITY,
        MeasurementValueOrigin.MEASURED,
        MaterialStateId.newId(),
        new MeasurementEvidenceReference(newUuid(), newUuid()),
        Instant.parse("2026-07-24T00:00:00Z"),
        new MeasurementProvenanceView(MeasurementSourceType.USER, null, null, null, null),
        MeasurementFreshnessStatus.CURRENT
    );
  }
}
