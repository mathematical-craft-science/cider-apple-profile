package org.craft.mcs.cap.core.cideranalysis.classification.domain.profile.longashton;

import java.util.function.Supplier;
import java.util.stream.Stream;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.core.cideranalysis.classification.domain.profile
    .BuiltInCiderClassificationThresholds.longAshton7;
import static org.craft.mcs.cap.core.cideranalysis.classification.domain.rule.ClassificationValueRange.atLeast;
import static org.craft.mcs.cap.core.cideranalysis.classification.domain.rule.ClassificationValueRange.between;
import static org.craft.mcs.cap.core.cideranalysis.classification.domain.rule.ClassificationValueRange.lessThan;
import static org.junit.jupiter.params.provider.Arguments.arguments;


class LongAshtonThresholdsTest {

  @ParameterizedTest(name = "rejects discontinuous {0} partition")
  @MethodSource("discontinuousLongAshton7Partitions")
  void rejectsDiscontinuousLongAshton7Partition(
      @NonNull String expectedName,
      @NonNull Supplier<LongAshton7Thresholds> constructor
  ) {
    assertThatThrownBy(constructor::get)
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage(expectedName + " ranges must form a contiguous partition");
  }

  @ParameterizedTest(name = "rejects discontinuous Long Ashton 4 {0} partition")
  @MethodSource("discontinuousLongAshton4Partitions")
  void rejectsDiscontinuousLongAshton4Partition(
      @NonNull String expectedAxis,
      @NonNull Supplier<LongAshton4Thresholds> constructor
  ) {
    assertThatThrownBy(constructor::get)
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("Long Ashton 4 " + expectedAxis + " ranges must form a contiguous partition");
  }

  private static @NonNull Stream<@NonNull Arguments> discontinuousLongAshton7Partitions() {
    final var builtIn = longAshton7();
    return Stream.of(
        arguments("Long Ashton 7 acidity", (Supplier<LongAshton7Thresholds>) () -> new LongAshton7Thresholds(
            lessThan("4.5"),
            atLeast("5.0"),
            builtIn.veryLowTannin(),
            builtIn.lowTannin(),
            builtIn.mediumTannin(),
            builtIn.highTannin(),
            builtIn.sharpLowTannin(),
            builtIn.sharpMediumTannin(),
            builtIn.sharpHighTannin()
        )),
        arguments(
            "Long Ashton 7 low-acidity tannin",
            (Supplier<LongAshton7Thresholds>) () -> new LongAshton7Thresholds(
                builtIn.lowAcid(),
                builtIn.highAcid(),
                lessThan("1000"),
                between("1100", "1500"),
                builtIn.mediumTannin(),
                builtIn.highTannin(),
                builtIn.sharpLowTannin(),
                builtIn.sharpMediumTannin(),
                builtIn.sharpHighTannin()
            )
        ),
        arguments(
            "Long Ashton 7 high-acidity tannin",
            (Supplier<LongAshton7Thresholds>) () -> new LongAshton7Thresholds(
                builtIn.lowAcid(),
                builtIn.highAcid(),
                builtIn.veryLowTannin(),
                builtIn.lowTannin(),
                builtIn.mediumTannin(),
                builtIn.highTannin(),
                lessThan("2000"),
                between("2100", "3000"),
                atLeast("3000")
            )
        )
    );
  }

  private static @NonNull Stream<@NonNull Arguments> discontinuousLongAshton4Partitions() {
    return Stream.of(
        arguments("acidity", (Supplier<LongAshton4Thresholds>) () -> new LongAshton4Thresholds(
            lessThan("4.5"),
            atLeast("5.0"),
            lessThan("2000"),
            atLeast("2000")
        )),
        arguments("tannin", (Supplier<LongAshton4Thresholds>) () -> new LongAshton4Thresholds(
            lessThan("4.5"),
            atLeast("4.5"),
            lessThan("2000"),
            atLeast("2100")
        ))
    );
  }
}
