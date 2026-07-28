package org.craft.mcs.cap.shared.kernel.chemical.axis;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class AxisValueTest {

  @Test
  void createsPointAndCalculatedValuesWithoutBounds() {
    final var point = AxisValue.point(ChemicalAxis.SPECIFIC_GRAVITY, new BigDecimal("1.050"));
    final var calculated = AxisValue.calculated(ChemicalAxis.TITRATABLE_ACIDITY, new BigDecimal("6.2"));

    assertThat(point)
        .returns(AxisValueKind.POINT, AxisValue::valueKind)
        .returns(null, AxisValue::min)
        .returns(null, AxisValue::max);
    assertThat(calculated)
        .returns(AxisValueKind.CALCULATED, AxisValue::valueKind)
        .returns(null, AxisValue::min)
        .returns(null, AxisValue::max);
  }

  @Test
  void createsExactIntervalMidpointWithItsBounds() {
    final var value = AxisValue.intervalMidpoint(
        ChemicalAxis.TITRATABLE_ACIDITY,
        new BigDecimal("4.1"),
        new BigDecimal("5.2")
    );

    assertThat(value)
        .returns(ChemicalAxis.TITRATABLE_ACIDITY, AxisValue::axis)
        .returns(new BigDecimal("4.65"), AxisValue::value)
        .returns(AxisValueKind.INTERVAL_MIDPOINT, AxisValue::valueKind)
        .returns(new BigDecimal("4.1"), AxisValue::min)
        .returns(new BigDecimal("5.2"), AxisValue::max);
  }

  @Test
  void rejectsIntervalCoordinateThatIsNotItsMidpoint() {
    assertThatThrownBy(() -> new AxisValue(
        ChemicalAxis.TITRATABLE_ACIDITY,
        new BigDecimal("4.7"),
        AxisValueKind.INTERVAL_MIDPOINT,
        new BigDecimal("4.1"),
        new BigDecimal("5.2")
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("axis interval value must equal the interval midpoint");
  }

  @Test
  void rejectsBoundsForPointValue() {
    assertThatThrownBy(() -> new AxisValue(
        ChemicalAxis.SPECIFIC_GRAVITY,
        new BigDecimal("1.050"),
        AxisValueKind.POINT,
        new BigDecimal("1.040"),
        new BigDecimal("1.060")
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("point and calculated axis values cannot declare interval bounds");
  }

  @Test
  void rejectsMissingOrReversedIntervalBounds() {
    assertThatThrownBy(() -> new AxisValue(
        ChemicalAxis.TITRATABLE_ACIDITY,
        new BigDecimal("4.5"),
        AxisValueKind.INTERVAL_MIDPOINT,
        null,
        new BigDecimal("5.0")
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("interval midpoint axis value requires both bounds");

    assertThatThrownBy(() -> AxisValue.intervalMidpoint(
        ChemicalAxis.TITRATABLE_ACIDITY,
        new BigDecimal("5.0"),
        new BigDecimal("4.0")
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("axis interval minimum cannot exceed maximum");
  }
}
