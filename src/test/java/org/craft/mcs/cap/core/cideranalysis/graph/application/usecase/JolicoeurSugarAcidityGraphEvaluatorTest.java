package org.craft.mcs.cap.core.cideranalysis.graph.application.usecase;

import org.craft.mcs.cap.core.cideranalysis.graph.application.contract.JolicoeurSugarAcidityGraphEvaluation;
import org.craft.mcs.cap.core.cideranalysis.graph.application.contract.JolicoeurSugarAcidityGraphEvaluation.MissingCharacteristic;
import org.craft.mcs.cap.core.cideranalysis.graph.application.contract.JolicoeurSugarAcidityGraphEvaluation.Point;
import org.craft.mcs.cap.core.cideranalysis.graph.application.contract.JolicoeurSugarAcidityZoneCode;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry.MidpointGraphRepresentativeValuePolicy;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.zone.BuiltInJolicoeurSugarAcidityZoneProfiles;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.zone.GeometryJolicoeurSugarAcidityZoneAssessor;
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
        .returns(SpecificGravityValue.of("1.060"), Point::specificGravity)
        .returns(
            TitratableAcidityValue.of("5.0"),
            Point::titratableAcidity
    );
    assertThat(result.zoneAssessment().zone()).isEqualTo(JolicoeurSugarAcidityZoneCode.PERFECT_APPLES);
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
        .returns(SpecificGravityValue.of("1.055"), Point::specificGravity)
        .returns(
            TitratableAcidityValue.of("5.0"),
            Point::titratableAcidity
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
        .containsExactly(MissingCharacteristic.TITRATABLE_ACIDITY);
  }
}
