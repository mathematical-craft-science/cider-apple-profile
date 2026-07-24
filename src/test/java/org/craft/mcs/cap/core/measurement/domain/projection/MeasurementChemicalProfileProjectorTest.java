package org.craft.mcs.cap.core.measurement.domain.projection;

import java.math.BigDecimal;
import java.util.Map;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCharacteristic;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.testing.MeasurementTestValues.value;


class MeasurementChemicalProfileProjectorTest {

  @Test
  void projectsPointAndCanonicalIntervalWithoutPersistingAMidpoint() {
    final Map<MeasurementCharacteristic, CharacteristicValue<MeasuredValue>> values = Map.of(
        MeasurementCharacteristic.SPECIFIC_GRAVITY,
        point(MeasurementCharacteristic.SPECIFIC_GRAVITY, "1.052"),
        MeasurementCharacteristic.TITRATABLE_ACIDITY,
        interval(MeasurementCharacteristic.TITRATABLE_ACIDITY, "5.5", "6.5")
    );
    final var profile = MeasurementChemicalProfileProjector.project(values).orElseThrow();

    assertThat(profile)
        .satisfies(value -> assertThat(value.sugar())
            .containsInstanceOf(PointCharacteristicValue.class))
        .satisfies(value -> assertThat(value.titratableAcidity().orElseThrow())
            .isInstanceOf(IntervalCharacteristicValue.class));
    assertThat(new ChemicalProfileAxisExtractor().extract(profile).requireValueOf(ChemicalAxis.ACIDITY).value())
        .isEqualByComparingTo("6.0");
  }

  @Test
  void ignoresCharacteristicsThatDoNotBelongToChemicalProfile() {
    final Map<MeasurementCharacteristic, CharacteristicValue<MeasuredValue>> values = Map.of(
        MeasurementCharacteristic.TEMPERATURE,
        point(MeasurementCharacteristic.TEMPERATURE, "18.0")
    );

    assertThat(MeasurementChemicalProfileProjector.project(values)).isEmpty();
  }

  @Test
  void rejectsAValueThatDoesNotMatchItsChemicalCharacteristic() {
    final MeasuredValue ph = PhValue.of("3.40");
    final var phValue = PointCharacteristicValue.of(ph);
    final Map<MeasurementCharacteristic, CharacteristicValue<MeasuredValue>> values = Map.of(
        MeasurementCharacteristic.SPECIFIC_GRAVITY,
        phValue
    );

    assertThatThrownBy(() -> MeasurementChemicalProfileProjector.project(values))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("measurement value type does not match SPECIFIC_GRAVITY");
  }

  private static PointCharacteristicValue<MeasuredValue> point(
      MeasurementCharacteristic characteristic,
      String value
  ) {
    return PointCharacteristicValue.of(value(characteristic, new BigDecimal(value)));
  }

  private static IntervalCharacteristicValue<MeasuredValue> interval(
      MeasurementCharacteristic characteristic,
      String lower,
      String upper
  ) {
    return IntervalCharacteristicValue.of(
        value(characteristic, new BigDecimal(lower)),
        value(characteristic, new BigDecimal(upper))
    );
  }
}
