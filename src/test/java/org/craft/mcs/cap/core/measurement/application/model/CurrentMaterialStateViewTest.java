package org.craft.mcs.cap.core.measurement.application.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementProvenance;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.application.measurement.MeasurementFreshnessStatus;
import org.craft.mcs.cap.shared.application.measurement.MeasurementValueOrigin;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.shared.testing.MeasurementTestValues;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;


class CurrentMaterialStateViewTest {

  @Test
  void preservesConcretePointAndIntervalValueShapes() {
    final var view = view(
        characteristic(MeasurementCharacteristic.SPECIFIC_GRAVITY, "1.050", null),
        characteristic(MeasurementCharacteristic.PH, "3.20", "3.40")
    );

    assertThat(view.value(MeasurementCharacteristic.SPECIFIC_GRAVITY, SpecificGravityValue.class))
        .contains(PointCharacteristicValue.of(SpecificGravityValue.of("1.050")));
    assertThat(view.value(MeasurementCharacteristic.PH, PhValue.class))
        .contains(IntervalCharacteristicValue.of(PhValue.of("3.20"), PhValue.of("3.40")));
  }

  @Test
  void rejectsDuplicateCurrentValuesForOneCharacteristic() {
    final var first = characteristic(MeasurementCharacteristic.PH, "3.20", null);
    final var second = characteristic(MeasurementCharacteristic.PH, "3.40", null);

    assertThatThrownBy(() -> view(first, second))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("current material state must contain at most one value per characteristic");
  }

  @Test
  void rejectsRequestedValueTypeThatDoesNotMatchTheCharacteristic() {
    final var view = view(characteristic(MeasurementCharacteristic.PH, "3.20", null));

    assertThatThrownBy(() -> view.value(MeasurementCharacteristic.PH, SpecificGravityValue.class))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessageContaining("PH has incompatible value type");
  }

  @Test
  void rejectsCharacteristicWhoseValueHasAnotherCanonicalType() {
    assertThatThrownBy(() -> new CurrentMaterialCharacteristicView(
        MeasurementCharacteristic.PH,
        MeasurementTestValues.point(MeasurementCharacteristic.SPECIFIC_GRAVITY, new BigDecimal("1.050")),
        MeasurementValueOrigin.MEASURED,
        MaterialStateId.newId(),
        MeasurementSetId.newId(),
        MeasurementRecordId.newId(),
        Instant.parse("2026-07-24T10:00:00Z"),
        MeasurementProvenance.derived(),
        MeasurementFreshnessStatus.CURRENT
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessageContaining("PH has an incompatible value");
  }

  @Test
  void rejectsRawEvidenceWhoseValueHasAnotherCanonicalType() {
    assertThatThrownBy(() -> new MeasurementResultEvidenceView(
        new MeasurementSubjectReference(MeasurementSubjectType.FEEDSTOCK_BATCH, newUuid()),
        MaterialStateId.newId(),
        MeasurementSetId.newId(),
        MeasurementRecordId.newId(),
        MeasurementCharacteristic.PH,
        MeasurementTestValues.point(MeasurementCharacteristic.SPECIFIC_GRAVITY, new BigDecimal("1.050")),
        Instant.parse("2026-07-24T10:00:00Z"),
        MeasurementProvenance.derived()
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessageContaining("PH has an incompatible value");
  }

  private static CurrentMaterialStateView view(CurrentMaterialCharacteristicView... characteristics) {
    return new CurrentMaterialStateView(
        new MeasurementSubjectReference(MeasurementSubjectType.FEEDSTOCK_BATCH, newUuid()),
        MaterialStateId.newId(),
        List.of(characteristics)
    );
  }

  private static CurrentMaterialCharacteristicView characteristic(
      MeasurementCharacteristic characteristic,
      String pointOrLower,
      String upper
  ) {
    return new CurrentMaterialCharacteristicView(
        characteristic,
        upper == null
            ? MeasurementTestValues.point(characteristic, new BigDecimal(pointOrLower))
            : MeasurementTestValues.interval(
                characteristic,
                new BigDecimal(pointOrLower),
                new BigDecimal(upper)
            ),
        MeasurementValueOrigin.MEASURED,
        MaterialStateId.newId(),
        MeasurementSetId.newId(),
        MeasurementRecordId.newId(),
        Instant.parse("2026-07-24T10:00:00Z"),
        MeasurementProvenance.derived(),
        MeasurementFreshnessStatus.CURRENT
    );
  }
}
