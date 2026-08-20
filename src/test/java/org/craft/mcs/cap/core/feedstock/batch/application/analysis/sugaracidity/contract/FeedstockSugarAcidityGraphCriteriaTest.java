package org.craft.mcs.cap.core.feedstock.batch.application.analysis.sugaracidity.contract;

import java.util.Set;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class FeedstockSugarAcidityGraphCriteriaTest {

  @Test
  void matchesRequestedPlottabilityAndZone() {
    final var criteria = new FeedstockSugarAcidityGraphCriteria(
        Set.of(FeedstockSugarAcidityGraphView.Zone.PERFECT_APPLES),
        true
    );

    assertThat(criteria.matches(plottable(FeedstockSugarAcidityGraphView.Zone.PERFECT_APPLES))).isTrue();
    assertThat(criteria.matches(plottable(FeedstockSugarAcidityGraphView.Zone.LOW_SUGAR_APPLES))).isFalse();
  }

  @Test
  void matchesUnplottableEvaluationWithoutZoneAssessment() {
    final var criteria = new FeedstockSugarAcidityGraphCriteria(Set.of(), false);
    final var evaluation = new FeedstockSugarAcidityGraphView(
        Set.of(FeedstockSugarAcidityGraphView.MissingCharacteristic.TITRATABLE_ACIDITY),
        null,
        null
    );

    assertThat(criteria.matches(evaluation)).isTrue();
  }

  private static FeedstockSugarAcidityGraphView plottable(FeedstockSugarAcidityGraphView.Zone zone) {
    return new FeedstockSugarAcidityGraphView(
        Set.of(),
        new FeedstockSugarAcidityGraphView.Point(
            SpecificGravityValue.of("1.060"),
            TitratableAcidityValue.of("6.0")
        ),
        new FeedstockSugarAcidityGraphView.ZoneAssessment(zone)
    );
  }
}
