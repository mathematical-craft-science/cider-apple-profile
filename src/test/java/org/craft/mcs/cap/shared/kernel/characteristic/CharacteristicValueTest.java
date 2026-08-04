package org.craft.mcs.cap.shared.kernel.characteristic;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class CharacteristicValueTest {

  @Test
  void pointValueRequiresValue() {
    assertThatNullPointerException()
        .isThrownBy(() -> PointCharacteristicValue.of(null));
  }

  @Test
  void intervalRequiresMinimumNotGreaterThanMaximum() {
    assertThatThrownBy(() -> IntervalCharacteristicValue.of(
        TitratableAcidityValue.of("6.0"),
        TitratableAcidityValue.of("5.0")
    )).isInstanceOf(DomainRuleViolationException.class);
  }

  @Test
  void intervalRequiresBoundsOfTheSameCharacteristicType() {
    final MeasuredValue ph = PhValue.of("3.4");
    final MeasuredValue tannin = TanninValue.of("1200");

    assertThatThrownBy(() -> IntervalCharacteristicValue.of(ph, tannin))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("characteristic interval bounds must have the same value type");
  }
}
