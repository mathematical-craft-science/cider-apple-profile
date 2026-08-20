package org.craft.mcs.cap.core.cideranalysis.graph.application.contract;

import java.util.Set;
import org.craft.mcs.cap.core.cideranalysis.graph.application.contract.JolicoeurSugarAcidityGraphEvaluation.MissingCharacteristic;
import org.craft.mcs.cap.core.cideranalysis.graph.application.contract.JolicoeurSugarAcidityGraphEvaluation.Point;
import org.craft.mcs.cap.core.cideranalysis.graph.application.contract.JolicoeurSugarAcidityGraphEvaluation.ZoneAssessment;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


class JolicoeurSugarAcidityGraphEvaluationTest {

  private static final Point POINT = new Point(
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
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("plottable graph evaluation requires point and zone assessment");
  }

  @Test
  void rejectsGraphResultWhenCharacteristicsAreMissing() {
    assertThatThrownBy(() -> new JolicoeurSugarAcidityGraphEvaluation(
        Set.of(MissingCharacteristic.TITRATABLE_ACIDITY),
        POINT,
        new ZoneAssessment(null)
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("unplottable graph evaluation cannot contain graph results");
  }
}
