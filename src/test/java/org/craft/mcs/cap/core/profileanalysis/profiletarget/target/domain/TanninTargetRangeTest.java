package org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain;

import java.util.List;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class TanninTargetRangeTest {

  private final TanninTargetRange range = new TanninTargetRange(
      TanninValue.of("1000"),
      TanninValue.of("1500")
  );

  @Test
  void includesBothTargetBoundaries() {
    assertThat(range)
        .matches(candidate -> candidate.contains(TanninValue.of("1000")))
        .matches(candidate -> candidate.contains(TanninValue.of("1500")))
        .matches(candidate -> !candidate.contains(TanninValue.of("999")));
  }

  @Test
  void returnsNearestTanninTargetValue() {
    assertThat(List.of(
        range.nearestValueTo(TanninValue.of("900")),
        range.nearestValueTo(TanninValue.of("1200")),
        range.nearestValueTo(TanninValue.of("1600"))
    )).containsExactly(
        TanninValue.of("1000"),
        TanninValue.of("1200"),
        TanninValue.of("1500")
    );
  }

  @Test
  void rejectsInvertedRange() {
    assertThatThrownBy(() -> new TanninTargetRange(
        TanninValue.of("1500"),
        TanninValue.of("1000")
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("measured value range minimum must precede its maximum");
  }

  @Test
  void rejectsHalfOpenTargetRange() {
    final var halfOpenRange = MeasuredValueRange.closedOpen(
        TanninValue.of("1000"),
        TanninValue.of("1500")
    );

    assertThatThrownBy(() -> new TanninTargetRange(halfOpenRange))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("tannin target range must include its maximum");
  }
}
