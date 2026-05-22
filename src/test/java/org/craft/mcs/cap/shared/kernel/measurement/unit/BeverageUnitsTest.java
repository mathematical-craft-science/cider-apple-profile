package org.craft.mcs.cap.shared.kernel.measurement.unit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class BeverageUnitsTest {

  @Test
  void keepsSemanticLabelsForSamePhysicalDimensionUnits() {
    assertThat(BeverageUnits.label(BeverageUnits.MILLIGRAM_PER_LITER_AS_TANNIC_ACID))
        .isEqualTo("mg/L TAE");
    assertThat(BeverageUnits.label(BeverageUnits.YAN))
        .isEqualTo("mg N/L");
  }
}
