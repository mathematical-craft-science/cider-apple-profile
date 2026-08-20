package org.craft.mcs.cap.core.formulation.calculation.application.contract;

import java.math.BigDecimal;
import java.util.Set;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTargetDistanceView.EvaluationAxis;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTargetDistanceView.FitStatus;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTargetDistanceView.Status;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


class FormulationTargetDistanceViewTest {

  @Test
  void rejectsIncompleteScoredDistance() {
    assertThatThrownBy(() -> distance(
        FormulationTargetDistanceView.Status.SCORED,
        null,
        100,
        FormulationTargetDistanceView.FitStatus.EXCELLENT,
        true,
        true,
        Set.of()
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("scored formulation target distance requires complete scoring facts");
  }

  @Test
  void rejectsScoringFactsForUnscorableDistance() {
    assertThatThrownBy(() -> distance(
        FormulationTargetDistanceView.Status.UNSCORABLE,
        BigDecimal.ZERO,
        null,
        FormulationTargetDistanceView.FitStatus.UNSCORABLE,
        true,
        false,
        Set.of(FormulationTargetDistanceView.EvaluationAxis.TANNIN)
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("unscorable formulation target distance requires missing axes only");
  }

  private static FormulationTargetDistanceView distance(
      @NonNull Status status,
      @Nullable BigDecimal value,
      @Nullable Integer matchPercent,
      @NonNull FitStatus fitStatus,
      boolean insideSugarAcidityTarget,
      boolean insideTarget,
      @NonNull Set<@NonNull EvaluationAxis> missingAxes
  ) {
    final var coordinates = new FormulationTargetDistanceView.Coordinates(
        SpecificGravityValue.of("1.050"),
        TitratableAcidityValue.of("6.0")
    );
    return new FormulationTargetDistanceView(
        status,
        coordinates,
        coordinates,
        new FormulationTargetDistanceView.TargetPosition(
            FormulationTargetDistanceView.AxisPosition.WITHIN,
            FormulationTargetDistanceView.AxisPosition.WITHIN
        ),
        null,
        null,
        value,
        matchPercent,
        fitStatus,
        insideSugarAcidityTarget,
        insideTarget,
        FormulationTargetDefinition.ObjectiveFunction.DISTANCE_TO_TARGET_AREA,
        FormulationTargetDistanceView.Metric.NORMALIZED_WEIGHTED_EUCLIDEAN,
        new FormulationTargetDefinition.Weights(BigDecimal.ONE, BigDecimal.ONE, null),
        FormulationTargetDistanceView.TargetKind.CUSTOM_RECTANGLE,
        FormulationTargetDistanceView.GeometryShape.RECTANGLE,
        missingAxes
    );
  }
}
