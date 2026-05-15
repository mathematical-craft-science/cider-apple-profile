package org.craft.mcs.cap.shared.kernel.chemical.value;

import org.craft.mcs.cap.shared.kernel.sugar.BrixValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ChemicalMeasuredValuesTest {

  @Test
  void acceptsBoundaryValues() {
    assertThat(SpecificGravityValue.of("1.000").asBigDecimal()).isEqualByComparingTo("1.000");
    assertThat(SpecificGravityValue.of("1.200").asBigDecimal()).isEqualByComparingTo("1.200");
    assertThat(BrixValue.of("0").asBigDecimal()).isEqualByComparingTo("0");
    assertThat(BrixValue.of("32.0").asBigDecimal()).isEqualByComparingTo("32.0");
    assertThat(TitratableAcidityValue.of("0").asBigDecimal()).isEqualByComparingTo("0");
    assertThat(TitratableAcidityValue.of("30.0").asBigDecimal()).isEqualByComparingTo("30.0");
    assertThat(TanninityValue.of("0").asBigDecimal()).isEqualByComparingTo("0");
    assertThat(TanninityValue.of("12000").asBigDecimal()).isEqualByComparingTo("12000");
    assertThat(PhValue.of("0").asBigDecimal()).isEqualByComparingTo("0");
    assertThat(PhValue.of("15.0").asBigDecimal()).isEqualByComparingTo("15.0");
  }

  @Test
  void rejectsOutOfRangeValues() {
    assertThatThrownBy(() -> SpecificGravityValue.of("0.999"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> SpecificGravityValue.of("1.201"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> BrixValue.of("-0.1"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> BrixValue.of("32.1"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> TitratableAcidityValue.of("-0.1"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> TanninityValue.of("12000.1"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> PhValue.of("15.1"))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
