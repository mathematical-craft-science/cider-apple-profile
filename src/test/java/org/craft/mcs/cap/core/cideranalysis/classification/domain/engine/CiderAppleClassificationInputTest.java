package org.craft.mcs.cap.core.cideranalysis.classification.domain.engine;

import java.util.Set;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class CiderAppleClassificationInputTest {

  @Test
  void rejectsEmptyInput() {
    assertThatThrownBy(() -> new CiderAppleClassificationInput(null, null, null))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("cider apple classification input cannot be empty");
  }

  @Test
  void reportsMissingCharacteristicsDeclaredByStrategy() {
    final var input = new CiderAppleClassificationInput(
        PointCharacteristicValue.of(SpecificGravityValue.of("1.050")),
        null,
        null
    );

    assertThat(input.missing(Set.of(
        ClassificationCharacteristic.SUGAR_CONTENT,
        ClassificationCharacteristic.ACIDITY,
        ClassificationCharacteristic.TANNIN
    )))
        .containsExactlyInAnyOrder(
            ClassificationCharacteristic.ACIDITY,
            ClassificationCharacteristic.TANNIN
        );
  }
}
