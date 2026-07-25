package org.craft.mcs.cap.core.measurement.domain.calculation;

import java.util.List;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ArithmeticMeanMeasurementDerivationTest {

  @Test
  void derivesArithmeticMeanInTheOriginalCharacteristicType() {
    final var result = ArithmeticMeanMeasurementDerivation.derive(List.of(
        PhValue.of("3.20"),
        PhValue.of("3.40"),
        PhValue.of("3.60")
    ));

    assertThat(result.value())
        .isInstanceOfSatisfying(
            PhValue.class,
            value -> assertThat(value.asBigDecimal()).isEqualByComparingTo("3.4")
        );
  }

  @Test
  void preservesAnIdenticalRepeatedValue() {
    final var result = ArithmeticMeanMeasurementDerivation.derive(List.of(
        SpecificGravityValue.of("1.050"),
        SpecificGravityValue.of("1.050")
    ));

    assertThat(result.value().asBigDecimal())
        .isEqualByComparingTo("1.050");
  }

  @Test
  void requiresAtLeastTwoObservations() {
    final var observations = List.<MeasuredValue>of(PhValue.of("3.20"));

    assertThatThrownBy(() -> ArithmeticMeanMeasurementDerivation.derive(observations))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("arithmetic mean requires at least two observations");
  }

  @Test
  void rejectsDifferentMeasurementCharacteristics() {
    final var observations = List.<MeasuredValue>of(
        PhValue.of("3.20"),
        SpecificGravityValue.of("1.050")
    );

    assertThatThrownBy(() -> ArithmeticMeanMeasurementDerivation.derive(observations))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("arithmetic mean observations must measure the same characteristic");
  }
}
