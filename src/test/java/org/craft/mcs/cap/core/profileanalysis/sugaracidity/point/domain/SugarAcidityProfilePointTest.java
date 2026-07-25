package org.craft.mcs.cap.core.profileanalysis.sugaracidity.point.domain;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


class SugarAcidityProfilePointTest {

  private static final AxisValue SUGAR = point(ChemicalAxis.SUGAR, "1.050");
  private static final AxisValue ACIDITY = point(ChemicalAxis.ACIDITY, "6.0");

  @Test
  void rejectsWrongSugarAxis() {
    assertThatThrownBy(() -> new SugarAcidityProfilePoint(ACIDITY, ACIDITY, null))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("sugar point must use SUGAR axis, but was ACIDITY");
  }

  @Test
  void rejectsWrongAcidityAxis() {
    assertThatThrownBy(() -> new SugarAcidityProfilePoint(SUGAR, SUGAR, null))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("acidity point must use ACIDITY axis, but was SUGAR");
  }

  @Test
  void rejectsWrongTanninAxis() {
    assertThatThrownBy(() -> new SugarAcidityProfilePoint(SUGAR, ACIDITY, SUGAR))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("tannin point must use TANNIN axis, but was SUGAR");
  }

  private static AxisValue point(
      ChemicalAxis chemicalAxis,
      String value
  ) {
    return AxisValue.point(chemicalAxis, new BigDecimal(value));
  }
}
