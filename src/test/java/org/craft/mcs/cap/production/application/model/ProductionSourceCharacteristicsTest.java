package org.craft.mcs.cap.production.application.model;

import org.craft.mcs.cap.core.production.application.model.ProductionSourceCharacteristics;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;


class ProductionSourceCharacteristicsTest {

  @Test
  void acceptsRequiredSourceCharacteristicsWithoutOptionalValues() {
    final var specificGravity = PointCharacteristicValue.of(SpecificGravityValue.of("1.052"));
    final var titratableAcidity = PointCharacteristicValue.of(TitratableAcidityValue.of("6.2"));

    assertThat(new ProductionSourceCharacteristics(specificGravity, titratableAcidity, null, null))
        .returns(specificGravity, ProductionSourceCharacteristics::specificGravity)
        .returns(titratableAcidity, ProductionSourceCharacteristics::titratableAcidity)
        .returns(null, ProductionSourceCharacteristics::tannin)
        .returns(null, ProductionSourceCharacteristics::yeastAssimilableNitrogen);
  }

  @Test
  void rejectsMissingRequiredSourceCharacteristics() {
    final var specificGravity = PointCharacteristicValue.of(SpecificGravityValue.of("1.052"));
    final var titratableAcidity = PointCharacteristicValue.of(TitratableAcidityValue.of("6.2"));

    assertThatNullPointerException()
        .isThrownBy(() -> new ProductionSourceCharacteristics(
            null,
            titratableAcidity,
            null,
            null
        ));
    assertThatNullPointerException()
        .isThrownBy(() -> new ProductionSourceCharacteristics(
            specificGravity,
            (CharacteristicValue<TitratableAcidityValue>) null,
            null,
            null
        ));
  }
}
