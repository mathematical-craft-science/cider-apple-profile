package org.craft.mcs.cap.shared.domain.measurement;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.domain.chemical.value.TitratableAcidityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class MeasurementTest {

  @Test
  void exactMeasurementRequiresValue() {
    assertThatNullPointerException()
        .isThrownBy(() -> ExactMeasurement.of(null));
  }

  @Test
  void rangeMeasurementRequiresMinNotGreaterThanMax() {
    assertThatThrownBy(() -> RangeMeasurement.of(
        TitratableAcidityValue.of("6.0"),
        TitratableAcidityValue.of("5.0")
    )).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void statisticalMeasurementValidatesStandardDeviationAndSampleSize() {
    assertThatThrownBy(() -> StatisticalMeasurement.of(
        TitratableAcidityValue.of("5.0"),
        null,
        new BigDecimal("-0.1"),
        2
    )).isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(() -> StatisticalMeasurement.of(
        TitratableAcidityValue.of("5.0"),
        null,
        BigDecimal.ZERO,
        0
    )).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void statisticalMeasurementKeepsOptionalStatistics() {
    final var measurement = StatisticalMeasurement.of(
        TitratableAcidityValue.of("5.0"),
        TitratableAcidityValue.of("4.8"),
        new BigDecimal("0.3"),
        12
    );

    assertThat(measurement.mean().asBigDecimal()).isEqualByComparingTo("5.0");
    assertThat(measurement.median()).isNotNull();
    assertThat(measurement.standardDeviation()).isEqualByComparingTo("0.3");
    assertThat(measurement.sampleSize()).isEqualTo(12);
  }
}
