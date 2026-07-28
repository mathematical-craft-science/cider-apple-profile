package org.craft.mcs.cap.core.profileanalysis.graph.application.service;

import org.craft.mcs.cap.core.profileanalysis.graph.application.model.JolicoeurSugarAcidityGraphEvaluation;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.GraphMissingCharacteristic;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.MidpointGraphRepresentativeValuePolicy;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityGraphPoint;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.BuiltInJolicoeurSugarAcidityZoneProfiles;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.GeometryJolicoeurSugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZone;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class JolicoeurSugarAcidityGraphEvaluatorTest {

  private final JolicoeurSugarAcidityGraphEvaluator evaluator =
      new JolicoeurSugarAcidityGraphEvaluator(
          new MidpointGraphRepresentativeValuePolicy(),
          new GeometryJolicoeurSugarAcidityZoneAssessor(
              BuiltInJolicoeurSugarAcidityZoneProfiles.rectangularZoneGeometryProfile()
          )
      );

  @Test
  void projectsTypedPointCharacteristicsWithoutChangingTheirValues() {
    final var result = evaluator.evaluate(
        PointCharacteristicValue.of(SpecificGravityValue.of("1.060")),
        PointCharacteristicValue.of(TitratableAcidityValue.of("5.0"))
    );

    assertThat(result)
        .returns(true, JolicoeurSugarAcidityGraphEvaluation::plottable)
        .returns(true, evaluation -> evaluation.missingCharacteristics().isEmpty());
    assertThat(result.point())
        .returns(SpecificGravityValue.of("1.060"), SugarAcidityGraphPoint::specificGravity)
        .returns(
            TitratableAcidityValue.of("5.0"),
            SugarAcidityGraphPoint::titratableAcidity
    );
    assertThat(result.zoneAssessment().zone()).isEqualTo(JolicoeurSugarAcidityZone.PERFECT_APPLES);
  }

  @Test
  void usesMidpointsOnlyForGraphCoordinates() {
    final var specificGravity = IntervalCharacteristicValue.of(
        SpecificGravityValue.of("1.050"),
        SpecificGravityValue.of("1.060")
    );
    final var titratableAcidity = IntervalCharacteristicValue.of(
        TitratableAcidityValue.of("4.0"),
        TitratableAcidityValue.of("6.0")
    );
    final var result = evaluator.evaluate(specificGravity, titratableAcidity);

    assertThat(result.point())
        .returns(SpecificGravityValue.of("1.055"), SugarAcidityGraphPoint::specificGravity)
        .returns(
            TitratableAcidityValue.of("5.0"),
            SugarAcidityGraphPoint::titratableAcidity
        );
  }

  @Test
  void reportsMissingCharacteristicsWithoutCreatingPartialGraphResults() {
    final var result = evaluator.evaluate(
        PointCharacteristicValue.of(SpecificGravityValue.of("1.050")),
        null
    );

    assertThat(result)
        .returns(false, JolicoeurSugarAcidityGraphEvaluation::plottable)
        .returns(null, JolicoeurSugarAcidityGraphEvaluation::point)
        .returns(null, JolicoeurSugarAcidityGraphEvaluation::zoneAssessment);
    assertThat(result.missingCharacteristics())
        .containsExactly(GraphMissingCharacteristic.TITRATABLE_ACIDITY);
  }
}
