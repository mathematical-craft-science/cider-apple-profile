package org.craft.mcs.cap.shared.kernel.measurement;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;

import org.craft.mcs.cap.shared.kernel.measurement.unit.BeverageUnits;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class VolumeValueTest {

  @Test
  void createsVolumeInLiters() {
    final var volume = VolumeValue.ofLiters("12.5");

    assertThat(volume.liters()).isEqualByComparingTo("12.5");
    assertThat(volume.unit()).isEqualTo(BeverageUnits.LITRE);
  }

  @Test
  void acceptsZeroVolume() {
    assertThat(VolumeValue.ofLiters("0").isZero()).isTrue();
  }

  @Test
  void rejectsNegativeVolume() {
    assertThatThrownBy(() -> VolumeValue.ofLiters("-0.1"))
        .isInstanceOf(DomainRuleViolationException.class);
  }

  @Test
  void keepsEqualityConsistentWithNumericComparison() {
    final var left = VolumeValue.ofLiters("12.50");
    final var right = VolumeValue.ofLiters("12.500");

    assertThat(left.compareTo(right)).isZero();
    assertThat(left).isEqualTo(right);
    assertThat(left).hasSameHashCodeAs(right);
  }
}
