package org.craft.mcs.cap.core.measurement.application.compatibility;

import java.math.BigDecimal;
import java.util.Map;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
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
  void projectsCanonicalPointValues() {
    final Map<MeasurementCharacteristic, CharacteristicValue<MeasuredValue>> values = Map.of(
        MeasurementCharacteristic.SPECIFIC_GRAVITY,
        point(MeasurementCharacteristic.SPECIFIC_GRAVITY, "1.052"),
        MeasurementCharacteristic.TITRATABLE_ACIDITY,
        point(MeasurementCharacteristic.TITRATABLE_ACIDITY, "6.0"),
        MeasurementCharacteristic.PH,
        point(MeasurementCharacteristic.PH, "3.40"),
        MeasurementCharacteristic.TANNIN,
        point(MeasurementCharacteristic.TANNIN, "1200"),
        MeasurementCharacteristic.YAN,
        point(MeasurementCharacteristic.YAN, "180")
    );
    final var profile = MeasurementChemicalProfileProjector.project(values).orElseThrow();

    assertThat(profile)
        .satisfies(value -> assertThat(value.sugar())
            .containsInstanceOf(PointCharacteristicValue.class))
        .satisfies(value -> assertThat(value.titratableAcidity().orElseThrow())
            .isInstanceOf(PointCharacteristicValue.class))
        .satisfies(value -> assertThat(value.ph())
            .containsInstanceOf(PointCharacteristicValue.class))
        .satisfies(value -> assertThat(value.tannin())
            .containsInstanceOf(PointCharacteristicValue.class))
        .satisfies(value -> assertThat(value.yeastAssimilableNitrogen())
            .containsInstanceOf(PointCharacteristicValue.class));
    assertThat(
        new ChemicalProfileAxisExtractor()
            .extract(profile)
            .requireValueOf(ChemicalAxis.TITRATABLE_ACIDITY)
            .value()
    )
        .isEqualByComparingTo("6.0");
  }

  @Test
  void projectsIntervalsForEveryChemicalCharacteristic() {
    final Map<MeasurementCharacteristic, CharacteristicValue<MeasuredValue>> values = Map.of(
        MeasurementCharacteristic.SPECIFIC_GRAVITY,
        interval(MeasurementCharacteristic.SPECIFIC_GRAVITY, "1.050", "1.054"),
        MeasurementCharacteristic.TITRATABLE_ACIDITY,
        interval(MeasurementCharacteristic.TITRATABLE_ACIDITY, "5.5", "6.5"),
        MeasurementCharacteristic.PH,
        interval(MeasurementCharacteristic.PH, "3.30", "3.50"),
        MeasurementCharacteristic.TANNIN,
        interval(MeasurementCharacteristic.TANNIN, "1000", "1400"),
        MeasurementCharacteristic.YAN,
        interval(MeasurementCharacteristic.YAN, "160", "200")
    );

    final var profile = MeasurementChemicalProfileProjector.project(values).orElseThrow();

    assertThat(profile)
        .satisfies(value -> assertThat(value.sugar())
            .containsInstanceOf(IntervalCharacteristicValue.class))
        .satisfies(value -> assertThat(value.titratableAcidity())
            .containsInstanceOf(IntervalCharacteristicValue.class))
        .satisfies(value -> assertThat(value.ph())
            .containsInstanceOf(IntervalCharacteristicValue.class))
        .satisfies(value -> assertThat(value.tannin())
            .containsInstanceOf(IntervalCharacteristicValue.class))
        .satisfies(value -> assertThat(value.yeastAssimilableNitrogen())
            .containsInstanceOf(IntervalCharacteristicValue.class));
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
