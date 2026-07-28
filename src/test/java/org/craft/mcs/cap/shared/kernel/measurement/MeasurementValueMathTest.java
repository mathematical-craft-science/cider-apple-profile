package org.craft.mcs.cap.shared.kernel.measurement;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class MeasurementValueMathTest {

  @Test
  void usesPointValueAsItsOwnMidpoint() {
    final var value = PointCharacteristicValue.of(PhValue.of("3.45"));

    assertThat(MeasurementValueMath.midpoint(value))
        .isEqualByComparingTo("3.45");
  }

  @Test
  void calculatesExactIntervalMidpointWithoutChangingEvidenceShape() {
    final var value = IntervalCharacteristicValue.of(
        PhValue.of("3.30"),
        PhValue.of("3.80")
    );

    assertThat(MeasurementValueMath.midpoint(value))
        .isEqualByComparingTo("3.55");
    assertThat(value)
        .isInstanceOf(IntervalCharacteristicValue.class);
  }

  @Test
  void calculatesMidpointForCanonicalDecimalBounds() {
    assertThat(MeasurementValueMath.midpoint(
        new BigDecimal("4.1"),
        new BigDecimal("5.2")
    )).isEqualByComparingTo("4.65");
  }
}
