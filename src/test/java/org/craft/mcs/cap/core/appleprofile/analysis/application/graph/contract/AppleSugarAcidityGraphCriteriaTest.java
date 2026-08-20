package org.craft.mcs.cap.core.appleprofile.analysis.application.graph.contract;

import java.util.Set;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class AppleSugarAcidityGraphCriteriaTest {

  @Test
  void matchesRequestedPlottabilityAndZone() {
    final var criteria = new AppleSugarAcidityGraphCriteria(
        Set.of(AppleSugarAcidityGraphView.Zone.PERFECT_APPLES),
        true
    );

    assertThat(criteria.matches(plottable(AppleSugarAcidityGraphView.Zone.PERFECT_APPLES))).isTrue();
    assertThat(criteria.matches(plottable(AppleSugarAcidityGraphView.Zone.LOW_SUGAR_APPLES))).isFalse();
  }

  @Test
  void matchesUnplottableEvaluationWithoutZoneAssessment() {
    final var criteria = new AppleSugarAcidityGraphCriteria(Set.of(), false);
    final var evaluation = new AppleSugarAcidityGraphView(
        Set.of(AppleSugarAcidityGraphView.MissingCharacteristic.TITRATABLE_ACIDITY),
        null,
        null
    );

    assertThat(criteria.matches(evaluation)).isTrue();
  }

  private static AppleSugarAcidityGraphView plottable(AppleSugarAcidityGraphView.Zone zone) {
    return new AppleSugarAcidityGraphView(
        Set.of(),
        new AppleSugarAcidityGraphView.Point(
            SpecificGravityValue.of("1.060"),
            TitratableAcidityValue.of("6.0")
        ),
        new AppleSugarAcidityGraphView.ZoneAssessment(zone)
    );
  }
}
