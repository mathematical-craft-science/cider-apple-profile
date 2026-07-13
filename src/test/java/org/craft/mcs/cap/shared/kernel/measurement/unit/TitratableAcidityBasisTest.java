package org.craft.mcs.cap.shared.kernel.measurement.unit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class TitratableAcidityBasisTest {

  @Test
  void mapsEachSupportedBasisToItsMeasurementUnit() {
    assertThat(TitratableAcidityBasis.values())
        .extracting(TitratableAcidityBasis::unit)
        .containsExactly(
            BeverageUnits.GRAM_PER_LITER_AS_MALIC_ACID,
            BeverageUnits.GRAM_PER_LITRE_AS_LACTIC_ACID,
            BeverageUnits.GRAM_PER_LITRE_AS_CITRIC_ACID
        );
  }
}
