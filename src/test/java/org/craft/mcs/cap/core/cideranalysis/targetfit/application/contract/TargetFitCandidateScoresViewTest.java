package org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract.TargetFitCandidateScoresView.AxisPosition;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract.TargetFitCandidateScoresView.Coordinates;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract.TargetFitCandidateScoresView.Distance;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract.TargetFitCandidateScoresView.DistanceStatus;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract.TargetFitCandidateScoresView.EvaluationAxis;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract.TargetFitCandidateScoresView.FitStatus;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract.TargetFitCandidateScoresView.TargetPosition;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


class TargetFitCandidateScoresViewTest {

  @Test
  void rejectsContributionWithoutAnyRole() {
    assertThatThrownBy(() -> new TargetFitCandidateScoresView.Contribution(
        List.of(),
        List.of(),
        Set.of()
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("target contribution requires at least one role");
  }

  @Test
  void rejectsIncompleteScoredDistance() {
    assertThatThrownBy(() -> distance(
        DistanceStatus.SCORED,
        null,
        100,
        FitStatus.EXCELLENT,
        true,
        true,
        Set.of()
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("scored target distance requires complete scoring facts");
  }

  @Test
  void rejectsScoringFactsForUnscorableDistance() {
    assertThatThrownBy(() -> distance(
        DistanceStatus.UNSCORABLE,
        BigDecimal.ZERO,
        null,
        FitStatus.UNSCORABLE,
        true,
        false,
        Set.of(EvaluationAxis.TANNIN)
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("unscorable target distance requires missing axes only");
  }

  private static Distance distance(
      @NonNull DistanceStatus status,
      @Nullable BigDecimal value,
      @Nullable Integer matchPercent,
      @NonNull FitStatus fitStatus,
      boolean insideSugarAcidityTarget,
      boolean insideTarget,
      @NonNull Set<@NonNull EvaluationAxis> missingAxes
  ) {
    final var coordinates = new Coordinates(
        SpecificGravityValue.of("1.050"),
        TitratableAcidityValue.of("6.0")
    );
    return new Distance(
        status,
        coordinates,
        coordinates,
        new TargetPosition(AxisPosition.WITHIN, AxisPosition.WITHIN),
        null,
        null,
        value,
        matchPercent,
        fitStatus,
        insideSugarAcidityTarget,
        insideTarget,
        TargetFitScoringConfiguration.ObjectiveFunction.DISTANCE_TO_TARGET_AREA,
        TargetFitCandidateScoresView.DistanceMetric.NORMALIZED_WEIGHTED_EUCLIDEAN,
        new TargetFitScoringConfiguration.ObjectiveWeights(BigDecimal.ONE, BigDecimal.ONE, null),
        TargetFitCandidateScoresView.TargetKind.CUSTOM_RECTANGLE,
        TargetFitCandidateScoresView.GeometryShape.RECTANGLE,
        missingAxes
    );
  }
}
