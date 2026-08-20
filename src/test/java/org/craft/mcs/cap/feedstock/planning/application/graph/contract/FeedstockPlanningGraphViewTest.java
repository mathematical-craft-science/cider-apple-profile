package org.craft.mcs.cap.feedstock.planning.application.graph.contract;

import java.util.Set;
import org.craft.mcs.cap.core.feedstock.planning.application.graph.contract.FeedstockPlanningGraphView;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeedstockPlanningGraphViewTest {

  @Test
  void rejectsPointWithoutZoneAssessmentWhenCharacteristicsAreMissing() {
    assertThatThrownBy(() -> new FeedstockPlanningGraphView(
        Set.of(FeedstockPlanningGraphView.MissingCharacteristic.TITRATABLE_ACIDITY),
        point(),
        null
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("feedstock planning graph evaluation has inconsistent shape");
  }

  @Test
  void rejectsZoneAssessmentWithoutPointWhenCharacteristicsAreMissing() {
    assertThatThrownBy(() -> new FeedstockPlanningGraphView(
        Set.of(FeedstockPlanningGraphView.MissingCharacteristic.SPECIFIC_GRAVITY),
        null,
        new FeedstockPlanningGraphView.ZoneAssessment(null)
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("feedstock planning graph evaluation has inconsistent shape");
  }

  private static FeedstockPlanningGraphView.@NonNull Point point() {
    return new FeedstockPlanningGraphView.Point(
        SpecificGravityValue.of("1.050"),
        TitratableAcidityValue.of("6.0")
    );
  }
}
