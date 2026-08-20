package org.craft.mcs.cap.feedstock.planning.application.profiletarget.contract;

import java.math.BigDecimal;
import java.util.Set;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetDistanceView;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


class FeedstockTargetDistanceViewTest {

  @Test
  void rejectsIncompleteScoredDistance() {
    assertThatThrownBy(() -> distance(
        FeedstockTargetDistanceView.Status.SCORED,
        null,
        100,
        FeedstockTargetDistanceView.FitStatus.EXCELLENT,
        true,
        true,
        Set.of()
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("scored feedstock target distance requires complete scoring facts");
  }

  @Test
  void rejectsScoringFactsForUnscorableDistance() {
    assertThatThrownBy(() -> distance(
        FeedstockTargetDistanceView.Status.UNSCORABLE,
        BigDecimal.ZERO,
        null,
        FeedstockTargetDistanceView.FitStatus.UNSCORABLE,
        true,
        false,
        Set.of(FeedstockTargetDistanceView.EvaluationAxis.TANNIN)
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("unscorable feedstock target distance requires missing axes only");
  }

  private static @NonNull FeedstockTargetDistanceView distance(
      FeedstockTargetDistanceView.@NonNull Status status,
      @Nullable BigDecimal value,
      @Nullable Integer matchPercent,
      FeedstockTargetDistanceView.@NonNull FitStatus fitStatus,
      boolean insideSugarAcidityTarget,
      boolean insideTarget,
      @NonNull Set<FeedstockTargetDistanceView.@NonNull EvaluationAxis> missingAxes
  ) {
    final var coordinates = new FeedstockTargetDistanceView.Coordinates(
        SpecificGravityValue.of("1.050"),
        TitratableAcidityValue.of("6.0")
    );
    return new FeedstockTargetDistanceView(
        status,
        coordinates,
        coordinates,
        null,
        null,
        value,
        matchPercent,
        fitStatus,
        insideSugarAcidityTarget,
        insideTarget,
        FeedstockTargetFitConfiguration.ObjectiveFunction.DISTANCE_TO_TARGET_AREA,
        FeedstockTargetDistanceView.Metric.NORMALIZED_WEIGHTED_EUCLIDEAN,
        FeedstockTargetDistanceView.TargetKind.CUSTOM_RECTANGLE,
        FeedstockTargetDistanceView.GeometryShape.RECTANGLE,
        missingAxes
    );
  }
}
