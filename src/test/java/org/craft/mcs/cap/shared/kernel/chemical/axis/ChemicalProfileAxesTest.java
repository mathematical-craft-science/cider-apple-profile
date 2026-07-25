package org.craft.mcs.cap.shared.kernel.chemical.axis;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ChemicalProfileAxesTest {

  @Test
  void returnsRequiredAxisValue() {
    final var sugar = new AxisValue(
        ChemicalAxis.SUGAR,
        BigDecimal.valueOf(1.055),
        AxisValueKind.POINT,
        null,
        null
    );
    final var axes = ChemicalProfileAxes.of(sugar, null, null);

    assertThat(axes.requireValueOf(ChemicalAxis.SUGAR))
        .isSameAs(sugar);
  }

  @Test
  void rejectsMissingRequiredAxis() {
    final var axes = ChemicalProfileAxes.of(null, null, null);

    assertThatThrownBy(() -> axes.requireValueOf(ChemicalAxis.SUGAR))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("required chemical axis is missing: SUGAR");
  }

  @Test
  void rejectsAxisValuePlacedIntoAnotherCharacteristicSlot() {
    final var acidity = AxisValue.point(ChemicalAxis.ACIDITY, BigDecimal.valueOf(5.2));

    assertThatThrownBy(() -> ChemicalProfileAxes.of(acidity, null, null))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("chemical profile axis slot SUGAR cannot contain ACIDITY");
  }
}
