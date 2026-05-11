package org.craft.mcs.cap.shared.kernel.measurement;

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
        .isInstanceOf(IllegalArgumentException.class);
  }
}
