package org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.normalization;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.ProfileEvaluationAxis;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ProfileAxisSensitivityUnitsTest {

  @Test
  void exposesVersionedCiderBlendingCalibrationInCanonicalUnits() {
    final var units = ProfileAxisSensitivityUnits.ciderBlendingV1();

    assertThat(units)
        .returns(new BigDecimal("0.005"), ProfileAxisSensitivityUnits::specificGravity)
        .returns(new BigDecimal("1.0"), ProfileAxisSensitivityUnits::titratableAcidity)
        .returns(new BigDecimal("250"), ProfileAxisSensitivityUnits::tannin);
    assertThat(units.unitFor(ProfileEvaluationAxis.TITRATABLE_ACIDITY))
        .isEqualByComparingTo("1.0");
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("nonPositiveSensitivityUnits")
  void rejectsNonPositiveSensitivityUnit(InvalidSensitivityUnit scenario) {
    assertThatThrownBy(scenario::create)
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("axis sensitivity unit must be positive: " + scenario.axis());
  }

  private static Object[][] nonPositiveSensitivityUnits() {
    return new Object[][]{
        {
            new InvalidSensitivityUnit(
                ProfileEvaluationAxis.SPECIFIC_GRAVITY,
                BigDecimal.ZERO,
                BigDecimal.ONE,
                BigDecimal.ONE
            )
        },
        {
            new InvalidSensitivityUnit(
                ProfileEvaluationAxis.TITRATABLE_ACIDITY,
                BigDecimal.ONE,
                new BigDecimal("-1"),
                BigDecimal.ONE
            )
        },
        {
            new InvalidSensitivityUnit(
                ProfileEvaluationAxis.TANNIN,
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ZERO
            )
        }
    };
  }

  private record InvalidSensitivityUnit(
      ProfileEvaluationAxis axis,
      BigDecimal specificGravity,
      BigDecimal titratableAcidity,
      BigDecimal tannin
  ) {

    private ProfileAxisSensitivityUnits create() {
      return new ProfileAxisSensitivityUnits(
          specificGravity,
          titratableAcidity,
          tannin
      );
    }
  }
}
