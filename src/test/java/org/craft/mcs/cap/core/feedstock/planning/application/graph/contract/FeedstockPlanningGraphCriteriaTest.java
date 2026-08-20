package org.craft.mcs.cap.core.feedstock.planning.application.graph.contract;

import java.util.Set;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class FeedstockPlanningGraphCriteriaTest {

  @Test
  void matchesRequestedPlottabilityAndZone() {
    final var criteria = new FeedstockPlanningGraphCriteria(
        Set.of(FeedstockPlanningGraphView.Zone.PERFECT_APPLES),
        true
    );

    assertThat(criteria.matches(plottable(FeedstockPlanningGraphView.Zone.PERFECT_APPLES))).isTrue();
    assertThat(criteria.matches(plottable(FeedstockPlanningGraphView.Zone.LOW_SUGAR_APPLES))).isFalse();
  }

  @Test
  void matchesUnplottableEvaluationWithoutZoneAssessment() {
    final var criteria = new FeedstockPlanningGraphCriteria(Set.of(), false);
    final var evaluation = new FeedstockPlanningGraphView(
        Set.of(FeedstockPlanningGraphView.MissingCharacteristic.TITRATABLE_ACIDITY),
        null,
        null
    );

    assertThat(criteria.matches(evaluation)).isTrue();
  }

  private static FeedstockPlanningGraphView plottable(FeedstockPlanningGraphView.Zone zone) {
    return new FeedstockPlanningGraphView(
        Set.of(),
        new FeedstockPlanningGraphView.Point(
            SpecificGravityValue.of("1.060"),
            TitratableAcidityValue.of("6.0")
        ),
        new FeedstockPlanningGraphView.ZoneAssessment(zone)
    );
  }
}
