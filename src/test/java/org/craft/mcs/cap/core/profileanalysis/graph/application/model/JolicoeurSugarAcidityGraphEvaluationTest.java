package org.craft.mcs.cap.core.profileanalysis.graph.application.model;

import java.util.Set;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.GraphMissingCharacteristic;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityGraphPoint;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZoneAssessment;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


class JolicoeurSugarAcidityGraphEvaluationTest {

  private static final SugarAcidityGraphPoint POINT = new SugarAcidityGraphPoint(
      SpecificGravityValue.of("1.060"),
      TitratableAcidityValue.of("5.0")
  );

  @Test
  void rejectsPartialPlottableResult() {
    assertThatThrownBy(() -> new JolicoeurSugarAcidityGraphEvaluation(
        Set.of(),
        POINT,
        null
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("plottable graph evaluation requires point and zone assessment");
  }

  @Test
  void rejectsGraphResultWhenCharacteristicsAreMissing() {
    assertThatThrownBy(() -> new JolicoeurSugarAcidityGraphEvaluation(
        Set.of(GraphMissingCharacteristic.TITRATABLE_ACIDITY),
        POINT,
        JolicoeurSugarAcidityZoneAssessment.unclassified()
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("unplottable graph evaluation cannot contain graph results");
  }
}
