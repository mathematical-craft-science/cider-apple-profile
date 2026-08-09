package org.craft.mcs.cap.core.profileanalysis.graph.application.model;

import java.util.Set;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.GraphMissingCharacteristic;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityGraphPoint;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZone;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZoneAssessment;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class JolicoeurSugarAcidityGraphCriteriaTest {

  @Test
  void matchesPlottableEvaluationInRequestedZone() {
    final var criteria = new JolicoeurSugarAcidityGraphCriteria(
        JolicoeurSugarAcidityZoneCriteria.of(JolicoeurSugarAcidityZone.PERFECT_APPLES),
        true
    );

    assertThat(criteria.matches(plottable(JolicoeurSugarAcidityZone.PERFECT_APPLES))).isTrue();
  }

  @Test
  void rejectsPlottableEvaluationFromAnotherZone() {
    final var criteria = new JolicoeurSugarAcidityGraphCriteria(
        JolicoeurSugarAcidityZoneCriteria.of(JolicoeurSugarAcidityZone.PERFECT_APPLES),
        true
    );

    assertThat(criteria.matches(plottable(JolicoeurSugarAcidityZone.LOW_SUGAR_APPLES))).isFalse();
  }

  @Test
  void matchesUnplottableEvaluationWithoutTryingToAssessZone() {
    final var criteria = new JolicoeurSugarAcidityGraphCriteria(null, false);
    final var evaluation = JolicoeurSugarAcidityGraphEvaluation.unplottable(
        Set.of(GraphMissingCharacteristic.TITRATABLE_ACIDITY)
    );

    assertThat(criteria.matches(evaluation)).isTrue();
  }

  private static JolicoeurSugarAcidityGraphEvaluation plottable(JolicoeurSugarAcidityZone zone) {
    return JolicoeurSugarAcidityGraphEvaluation.plottable(
        new SugarAcidityGraphPoint(
            SpecificGravityValue.of("1.060"),
            TitratableAcidityValue.of("6.0")
        ),
        JolicoeurSugarAcidityZoneAssessment.classified(zone)
    );
  }
}
