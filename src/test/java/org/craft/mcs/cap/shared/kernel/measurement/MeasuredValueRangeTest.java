package org.craft.mcs.cap.shared.kernel.measurement;

import java.util.List;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class MeasuredValueRangeTest {

  @Test
  void usesInclusiveLowerAndExclusiveUpperBoundary() {
    final var range = MeasuredValueRange.closedOpen(
        TitratableAcidityValue.of("4.5"),
        TitratableAcidityValue.of("7.5")
    );

    assertThat(List.of(
        range.contains(TitratableAcidityValue.of("4.5")),
        range.contains(TitratableAcidityValue.of("7.499")),
        range.contains(TitratableAcidityValue.of("7.5"))
    )).containsExactly(true, true, false);
    assertThat(List.of(
        range.positionOf(TitratableAcidityValue.of("4.499")),
        range.positionOf(TitratableAcidityValue.of("4.5")),
        range.positionOf(TitratableAcidityValue.of("7.499")),
        range.positionOf(TitratableAcidityValue.of("7.5"))
    )).containsExactly(
        MeasuredValueRangePosition.BELOW,
        MeasuredValueRangePosition.WITHIN,
        MeasuredValueRangePosition.WITHIN,
        MeasuredValueRangePosition.ABOVE
    );
  }

  @Test
  void closedRangeIncludesUpperBoundaryAndMayRepresentOneValue() {
    final var range = MeasuredValueRange.closed(
        TitratableAcidityValue.of("4.5"),
        TitratableAcidityValue.of("7.5")
    );
    final var pointRange = MeasuredValueRange.closed(
        TitratableAcidityValue.of("6.0"),
        TitratableAcidityValue.of("6.0")
    );

    assertThat(range.contains(TitratableAcidityValue.of("7.5"))).isTrue();
    assertThat(pointRange.contains(TitratableAcidityValue.of("6.0"))).isTrue();
  }

  @Test
  void projectsValuesOntoRangeClosure() {
    final var range = MeasuredValueRange.closedOpen(
        TitratableAcidityValue.of("4.5"),
        TitratableAcidityValue.of("7.5")
    );

    assertThat(List.of(
        range.projectOntoClosure(TitratableAcidityValue.of("4.0")),
        range.projectOntoClosure(TitratableAcidityValue.of("6.0")),
        range.projectOntoClosure(TitratableAcidityValue.of("8.0"))
    )).containsExactly(
        TitratableAcidityValue.of("4.5"),
        TitratableAcidityValue.of("6.0"),
        TitratableAcidityValue.of("7.5")
    );
  }

  @Test
  void rejectsEmptyHalfOpenRange() {
    final var boundary = TitratableAcidityValue.of("4.5");

    assertThatThrownBy(() -> MeasuredValueRange.closedOpen(boundary, boundary))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("measured value range minimum must precede its maximum");
  }

  @Test
  void rejectsInvertedRange() {
    assertThatThrownBy(() -> MeasuredValueRange.closed(
        TitratableAcidityValue.of("7.5"),
        TitratableAcidityValue.of("4.5")
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("measured value range minimum must precede its maximum");
  }

  @Test
  void rejectsBoundsFromDifferentCharacteristicsWhenGenericTypeIsErased() {
    assertThatThrownBy(() -> MeasuredValueRange.closed(
        SpecificGravityValue.of("1.050"),
        TitratableAcidityValue.of("5.0")
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("measured value range bounds must have the same measurement type");
  }
}
