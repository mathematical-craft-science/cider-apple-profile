package org.craft.mcs.cap.core.traceability.domain.model;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class BatchTraceMaterialCharacteristicsTest {

  @Test
  void retainsDirectIntervalWithoutCollapsingItToAnUntypedMidpoint() {
    final var interval = IntervalCharacteristicValue.of(
        SpecificGravityValue.of("1.048"),
        SpecificGravityValue.of("1.052")
    );
    final var characteristic = BatchTraceCharacteristicSnapshot.direct(interval);

    assertThat(characteristic)
        .isInstanceOfSatisfying(
            BatchTraceCharacteristicSnapshot.Direct.class,
            direct -> assertThat(direct.value()).isSameAs(interval)
        );
  }

  @Test
  void retainsCalculatedPointAsASeparateSealedVariant() {
    final var value = TitratableAcidityValue.of("6.2");
    final var characteristic = BatchTraceCharacteristicSnapshot.calculated(value);

    assertThat(characteristic)
        .isInstanceOfSatisfying(
            BatchTraceCharacteristicSnapshot.Calculated.class,
            calculated -> assertThat(calculated.value()).isSameAs(value)
        );
  }

  @Test
  void rejectsEmptyCharacteristicSnapshot() {
    assertThatThrownBy(() -> new BatchTraceMaterialCharacteristics(null, null, null, null))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("batch trace material characteristics must contain at least one value");
  }

  @Test
  void acceptsOneTypedCharacteristic() {
    final var specificGravity = BatchTraceCharacteristicSnapshot.direct(
        PointCharacteristicValue.of(SpecificGravityValue.of("1.052"))
    );

    assertThat(new BatchTraceMaterialCharacteristics(specificGravity, null, null, null))
        .returns(specificGravity, BatchTraceMaterialCharacteristics::specificGravity)
        .returns(null, BatchTraceMaterialCharacteristics::titratableAcidity)
        .returns(null, BatchTraceMaterialCharacteristics::tannin)
        .returns(null, BatchTraceMaterialCharacteristics::yeastAssimilableNitrogen);
  }
}
