package org.craft.mcs.cap.core.profileanalysis.graph.domain;

import java.util.List;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


class SugarAcidityGraphAxisScaleTest {

  @Test
  void rejectsEqualScaleBounds() {
    assertThatThrownBy(() -> new SugarAcidityGraphAxisScale<>(
        TitratableAcidityValue.of("1"),
        TitratableAcidityValue.of("1"),
        List.of()
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("graph axis scale minimum must be less than maximum");
  }

  @Test
  void rejectsBoundaryValuesAtTheScaleBoundary() {
    assertThatThrownBy(() -> new SugarAcidityGraphAxisScale<>(
        TitratableAcidityValue.of("0"),
        TitratableAcidityValue.of("10"),
        List.of(TitratableAcidityValue.of("0"))
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage(
            "graph axis scale boundary values must be strictly increasing inside its bounds"
        );
  }

  @Test
  void rejectsUnorderedBoundaryValues() {
    assertThatThrownBy(() -> new SugarAcidityGraphAxisScale<>(
        TitratableAcidityValue.of("0"),
        TitratableAcidityValue.of("10"),
        List.of(
            TitratableAcidityValue.of("2"),
            TitratableAcidityValue.of("1")
        )
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage(
            "graph axis scale boundary values must be strictly increasing inside its bounds"
        );
  }
}
