package org.craft.mcs.cap.feedstock.application.inventory.port.in;

import org.craft.mcs.cap.core.feedstock.application.inventory.port.in.FeedstockMaterialCharacteristics;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;


class FeedstockMaterialCharacteristicsTest {

  @Test
  void acceptsRequiredSourceCharacteristicsWithoutOptionalValues() {
    final var specificGravity = PointCharacteristicValue.of(SpecificGravityValue.of("1.052"));
    final var titratableAcidity = PointCharacteristicValue.of(TitratableAcidityValue.of("6.2"));

    assertThat(new FeedstockMaterialCharacteristics(specificGravity, titratableAcidity, null, null))
        .returns(specificGravity, FeedstockMaterialCharacteristics::specificGravity)
        .returns(titratableAcidity, FeedstockMaterialCharacteristics::titratableAcidity)
        .returns(null, FeedstockMaterialCharacteristics::tannin)
        .returns(null, FeedstockMaterialCharacteristics::yeastAssimilableNitrogen);
  }

  @Test
  void rejectsMissingRequiredSourceCharacteristics() {
    final var specificGravity = PointCharacteristicValue.of(SpecificGravityValue.of("1.052"));
    final var titratableAcidity = PointCharacteristicValue.of(TitratableAcidityValue.of("6.2"));

    assertThatNullPointerException()
        .isThrownBy(() -> new FeedstockMaterialCharacteristics(
            null,
            titratableAcidity,
            null,
            null
        ));
    assertThatNullPointerException()
        .isThrownBy(() -> new FeedstockMaterialCharacteristics(
            specificGravity,
            (CharacteristicValue<TitratableAcidityValue>) null,
            null,
            null
        ));
  }
}
