package org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican;

import org.craft.mcs.cap.core.profileanalysis.classification.domain.engine.CiderAppleClassificationInput;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.BuiltInCiderClassificationThresholds;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.rule.ClassificationValueRange;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class NorthAmericanProfileCalculatorTest {

  private final NorthAmericanProfileCalculator calculator = new NorthAmericanProfileCalculator(
      BuiltInCiderClassificationThresholds.northAmerican()
  );

  @Test
  void assessesEachCharacteristicUsingNorthAmericanRanges() {
    final var input = input("1.070", "11.0", "2500");

    assertThat(calculator.assessSugar(input.sugarContent()))
        .isEqualTo(NorthAmericanSugarLevel.VERY_HIGH);
    assertThat(calculator.assessAcidity(input.acidity()))
        .isEqualTo(NorthAmericanAcidityLevel.VERY_HIGH);
    assertThat(calculator.assessTannin(input.tannin()))
        .isEqualTo(NorthAmericanTanninLevel.HIGH);
  }

  @Test
  void calculatesCompleteNorthAmericanProfile() {
    assertThat(calculator.calculate(input("1.055", "6.0", "2000")))
        .returns(NorthAmericanSugarLevel.MEDIUM, NorthAmericanProfile::sugar)
        .returns(NorthAmericanAcidityLevel.MEDIUM, NorthAmericanProfile::acidity)
        .returns(NorthAmericanTanninLevel.MEDIUM, NorthAmericanProfile::tannin);
  }

  @ParameterizedTest
  @CsvSource({
      "1.045, MEDIUM",
      "1.060, HIGH",
      "1.070, VERY_HIGH"
  })
  void assignsExactSugarBoundaryToTheRangeStartingAtThatBoundary(
      String specificGravity,
      NorthAmericanSugarLevel expectedLevel
  ) {
    assertThat(calculator.assessSugar(input(specificGravity, "6.0", "2000").sugarContent()))
        .isEqualTo(expectedLevel);
  }

  @ParameterizedTest
  @CsvSource({
      "4.5, MEDIUM",
      "7.5, HIGH",
      "11.0, VERY_HIGH"
  })
  void assignsExactAcidityBoundaryToTheRangeStartingAtThatBoundary(
      String titratableAcidity,
      NorthAmericanAcidityLevel expectedLevel
  ) {
    assertThat(calculator.assessAcidity(input("1.055", titratableAcidity, "2000").acidity()))
        .isEqualTo(expectedLevel);
  }

  @ParameterizedTest
  @CsvSource({
      "1500, MEDIUM",
      "2500, HIGH"
  })
  void assignsExactTanninBoundaryToTheRangeStartingAtThatBoundary(
      String tannin,
      NorthAmericanTanninLevel expectedLevel
  ) {
    assertThat(calculator.assessTannin(input("1.055", "6.0", tannin).tannin()))
        .isEqualTo(expectedLevel);
  }

  @Test
  void rejectsThresholdRangesThatDoNotFormAContiguousPartition() {
    final var builtIn = BuiltInCiderClassificationThresholds.northAmerican();

    assertThatThrownBy(() -> new NorthAmericanThresholds(
        ClassificationValueRange.lessThan("1.045"),
        ClassificationValueRange.between("1.050", "1.060"),
        builtIn.highSugar(),
        builtIn.veryHighSugar(),
        builtIn.lowAcidity(),
        builtIn.mediumAcidity(),
        builtIn.highAcidity(),
        builtIn.veryHighAcidity(),
        builtIn.lowTannin(),
        builtIn.mediumTannin(),
        builtIn.highTannin()
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("North American sugar ranges must form a contiguous partition");
  }

  private static CiderAppleClassificationInput input(
      String specificGravity,
      String titratableAcidity,
      String tannin
  ) {
    return new CiderAppleClassificationInput(
        PointCharacteristicValue.of(SpecificGravityValue.of(specificGravity)),
        PointCharacteristicValue.of(TitratableAcidityValue.of(titratableAcidity)),
        PointCharacteristicValue.of(TanninValue.of(tannin))
    );
  }
}
