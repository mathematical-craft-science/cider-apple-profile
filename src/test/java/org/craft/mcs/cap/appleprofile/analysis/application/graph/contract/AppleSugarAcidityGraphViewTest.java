package org.craft.mcs.cap.appleprofile.analysis.application.graph.contract;

import java.util.Set;
import org.craft.mcs.cap.core.appleprofile.analysis.application.graph.contract.AppleSugarAcidityGraphView;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppleSugarAcidityGraphViewTest {

  @Test
  void rejectsPointWithoutZoneAssessmentWhenCharacteristicsAreMissing() {
    assertThatThrownBy(() -> new AppleSugarAcidityGraphView(
        Set.of(AppleSugarAcidityGraphView.MissingCharacteristic.TITRATABLE_ACIDITY),
        point(),
        null
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("apple graph evaluation has inconsistent shape");
  }

  @Test
  void rejectsZoneAssessmentWithoutPointWhenCharacteristicsAreMissing() {
    assertThatThrownBy(() -> new AppleSugarAcidityGraphView(
        Set.of(AppleSugarAcidityGraphView.MissingCharacteristic.SPECIFIC_GRAVITY),
        null,
        new AppleSugarAcidityGraphView.ZoneAssessment(null)
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("apple graph evaluation has inconsistent shape");
  }

  private static AppleSugarAcidityGraphView.@NonNull Point point() {
    return new AppleSugarAcidityGraphView.Point(
        SpecificGravityValue.of("1.050"),
        TitratableAcidityValue.of("6.0")
    );
  }
}
