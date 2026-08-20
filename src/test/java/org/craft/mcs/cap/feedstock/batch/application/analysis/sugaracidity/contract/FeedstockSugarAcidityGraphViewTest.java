package org.craft.mcs.cap.feedstock.batch.application.analysis.sugaracidity.contract;

import java.util.Set;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.sugaracidity.contract.FeedstockSugarAcidityGraphView;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeedstockSugarAcidityGraphViewTest {

  @Test
  void rejectsPointWithoutZoneAssessmentWhenCharacteristicsAreMissing() {
    assertThatThrownBy(() -> new FeedstockSugarAcidityGraphView(
        Set.of(FeedstockSugarAcidityGraphView.MissingCharacteristic.TITRATABLE_ACIDITY),
        point(),
        null
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("feedstock graph evaluation has inconsistent shape");
  }

  @Test
  void rejectsZoneAssessmentWithoutPointWhenCharacteristicsAreMissing() {
    assertThatThrownBy(() -> new FeedstockSugarAcidityGraphView(
        Set.of(FeedstockSugarAcidityGraphView.MissingCharacteristic.SPECIFIC_GRAVITY),
        null,
        new FeedstockSugarAcidityGraphView.ZoneAssessment(null)
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("feedstock graph evaluation has inconsistent shape");
  }

  private static FeedstockSugarAcidityGraphView.@NonNull Point point() {
    return new FeedstockSugarAcidityGraphView.Point(
        SpecificGravityValue.of("1.050"),
        TitratableAcidityValue.of("6.0")
    );
  }
}
