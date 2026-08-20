package org.craft.mcs.cap.core.production.planning.application.contract;

import java.math.BigDecimal;
import java.util.Set;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ProductionCandidateTargetDistanceViewTest {

  @Test
  void rejectsIncompleteScoredDistance() {
    assertThatThrownBy(() -> distance(
        ProductionCandidateTargetDistanceView.Status.SCORED,
        null,
        100,
        ProductionCandidateTargetDistanceView.FitStatus.EXCELLENT,
        true,
        true,
        Set.of()
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("scored production candidate target distance requires complete scoring facts");
  }

  @Test
  void rejectsScoringFactsForUnscorableDistance() {
    assertThatThrownBy(() -> distance(
        ProductionCandidateTargetDistanceView.Status.UNSCORABLE,
        BigDecimal.ZERO,
        null,
        ProductionCandidateTargetDistanceView.FitStatus.UNSCORABLE,
        true,
        false,
        Set.of(ProductionCandidateTargetDistanceView.EvaluationAxis.TANNIN)
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("unscorable production candidate target distance requires missing axes only");
  }

  @Test
  void rejectsNonPositiveWeights() {
    assertThatThrownBy(() -> new ProductionCandidateTargetDistanceView.Weights(
        BigDecimal.ZERO,
        BigDecimal.ONE,
        null
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("production candidate target weights must be positive");
  }

  private static @NonNull ProductionCandidateTargetDistanceView distance(
      ProductionCandidateTargetDistanceView.@NonNull Status status,
      @Nullable BigDecimal value,
      @Nullable Integer matchPercent,
      ProductionCandidateTargetDistanceView.@NonNull FitStatus fitStatus,
      boolean insideSugarAcidityTarget,
      boolean insideTarget,
      @NonNull Set<ProductionCandidateTargetDistanceView.@NonNull EvaluationAxis> missingAxes
  ) {
    final var coordinates = new ProductionCandidateTargetDistanceView.Coordinates(
        SpecificGravityValue.of("1.050"),
        TitratableAcidityValue.of("6.0")
    );
    return new ProductionCandidateTargetDistanceView(
        status,
        coordinates,
        coordinates,
        new ProductionCandidateTargetDistanceView.TargetPosition(
            ProductionCandidateTargetDistanceView.AxisPosition.WITHIN,
            ProductionCandidateTargetDistanceView.AxisPosition.WITHIN
        ),
        null,
        null,
        value,
        matchPercent,
        fitStatus,
        insideSugarAcidityTarget,
        insideTarget,
        ProductionBatchCandidateCalculationRequest.ObjectiveFunction.DISTANCE_TO_TARGET_AREA,
        ProductionCandidateTargetDistanceView.Metric.NORMALIZED_WEIGHTED_EUCLIDEAN,
        new ProductionCandidateTargetDistanceView.Weights(BigDecimal.ONE, BigDecimal.ONE, null),
        ProductionCandidateTargetDistanceView.TargetKind.CUSTOM_RECTANGLE,
        ProductionCandidateTargetDistanceView.GeometryShape.RECTANGLE,
        missingAxes
    );
  }
}
