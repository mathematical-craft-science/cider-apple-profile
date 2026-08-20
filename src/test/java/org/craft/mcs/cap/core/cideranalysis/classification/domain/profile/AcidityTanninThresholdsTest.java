package org.craft.mcs.cap.core.cideranalysis.classification.domain.profile;

import java.util.function.Supplier;
import java.util.stream.Stream;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.core.cideranalysis.classification.domain.rule.ClassificationValueRange.all;
import static org.craft.mcs.cap.core.cideranalysis.classification.domain.rule.ClassificationValueRange.atLeast;
import static org.craft.mcs.cap.core.cideranalysis.classification.domain.rule.ClassificationValueRange.between;
import static org.craft.mcs.cap.core.cideranalysis.classification.domain.rule.ClassificationValueRange.lessThan;
import static org.junit.jupiter.params.provider.Arguments.arguments;


class AcidityTanninThresholdsTest {

  @ParameterizedTest(name = "rejects null {0} range")
  @MethodSource("nullRanges")
  void rejectsNullRange(
      @NonNull String expectedMessage,
      @NonNull Supplier<AcidityTanninThresholds> constructor
  ) {
    assertThatNullPointerException()
        .isThrownBy(constructor::get)
        .withMessage(expectedMessage + " range cannot be null");
  }

  @Test
  void rejectsGapInAcidityPartition() {
    assertThatThrownBy(() -> new AcidityTanninThresholds(
        lessThan("4.0"),
        between("4.5", "6.0"),
        atLeast("6.0"),
        lessThan("2000"),
        between("2000", "3000"),
        atLeast("3000")
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("acidity ranges must form a contiguous partition");
  }

  @Test
  void rejectsOverlapInTanninPartition() {
    assertThatThrownBy(() -> new AcidityTanninThresholds(
        lessThan("4.0"),
        between("4.0", "6.0"),
        atLeast("6.0"),
        lessThan("2000"),
        between("1500", "3000"),
        atLeast("3000")
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("tannin ranges must form a contiguous partition");
  }

  private static @NonNull Stream<@NonNull Arguments> nullRanges() {
    final var range = all();
    return Stream.of(
        arguments("low acid", (Supplier<AcidityTanninThresholds>)
            () -> new AcidityTanninThresholds(null, range, range, range, range, range)),
        arguments("medium acid", (Supplier<AcidityTanninThresholds>)
            () -> new AcidityTanninThresholds(range, null, range, range, range, range)),
        arguments("high acid", (Supplier<AcidityTanninThresholds>)
            () -> new AcidityTanninThresholds(range, range, null, range, range, range)),
        arguments("low tannin", (Supplier<AcidityTanninThresholds>)
            () -> new AcidityTanninThresholds(range, range, range, null, range, range)),
        arguments("medium tannin", (Supplier<AcidityTanninThresholds>)
            () -> new AcidityTanninThresholds(range, range, range, range, null, range)),
        arguments("high tannin", (Supplier<AcidityTanninThresholds>)
            () -> new AcidityTanninThresholds(range, range, range, range, range, null))
    );
  }
}
