package org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring;

import java.util.Set;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry.SugarAcidityGraphPoint;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.target.SugarAcidityTargetPosition;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRangePosition;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ProfileTargetComparisonTest {

  @Test
  void rejectsDifferentFullContainmentWhenNoTanninTargetExists() {
    final var point = new SugarAcidityGraphPoint(
        SpecificGravityValue.of("1.055"),
        TitratableAcidityValue.of("5.5")
    );

    assertThatThrownBy(() -> new ProfileTargetComparison(
        point,
        point,
        new SugarAcidityTargetPosition(
            MeasuredValueRangePosition.WITHIN,
            MeasuredValueRangePosition.WITHIN
        ),
        null,
        null,
        true,
        false,
        Set.of()
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("target without tannin must use its SG/TA containment as full containment");
  }
}
