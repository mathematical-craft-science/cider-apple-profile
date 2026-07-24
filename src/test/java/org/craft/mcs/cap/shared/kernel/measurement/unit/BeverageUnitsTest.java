package org.craft.mcs.cap.shared.kernel.measurement.unit;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class BeverageUnitsTest {

  @Test
  void keepsSemanticLabelsForSamePhysicalDimensionUnits() {
    assertThat(List.of(
        BeverageUnits.label(BeverageUnits.MILLIGRAM_PER_LITER_AS_TANNIC_ACID),
        BeverageUnits.label(BeverageUnits.YAN),
        BeverageUnits.label(BeverageUnits.CELSIUS),
        BeverageUnits.label(BeverageUnits.NTU),
        BeverageUnits.label(BeverageUnits.FTU)
    )).containsExactly("mg/L TAE", "mg N/L", "°C", "NTU", "FTU");
  }
}
