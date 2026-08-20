package org.craft.mcs.cap.appleprofile.analysis.application.profiletarget.contract;

import java.math.BigDecimal;
import java.util.Set;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleTargetDistanceView;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


class AppleTargetDistanceViewTest {

  @Test
  void rejectsIncompleteScoredDistance() {
    assertThatThrownBy(() -> distance(
        AppleTargetDistanceView.Status.SCORED,
        null,
        100,
        AppleTargetDistanceView.FitStatus.EXCELLENT,
        true,
        true,
        Set.of()
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("scored apple target distance requires complete scoring facts");
  }

  @Test
  void rejectsScoringFactsForUnscorableDistance() {
    assertThatThrownBy(() -> distance(
        AppleTargetDistanceView.Status.UNSCORABLE,
        BigDecimal.ZERO,
        null,
        AppleTargetDistanceView.FitStatus.UNSCORABLE,
        true,
        false,
        Set.of(AppleTargetDistanceView.EvaluationAxis.TANNIN)
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("unscorable apple target distance requires missing axes only");
  }

  private static @NonNull AppleTargetDistanceView distance(
      AppleTargetDistanceView.@NonNull Status status,
      @Nullable BigDecimal value,
      @Nullable Integer matchPercent,
      AppleTargetDistanceView.@NonNull FitStatus fitStatus,
      boolean insideSugarAcidityTarget,
      boolean insideTarget,
      @NonNull Set<AppleTargetDistanceView.@NonNull EvaluationAxis> missingAxes
  ) {
    final var coordinates = new AppleTargetDistanceView.Coordinates(
        SpecificGravityValue.of("1.050"),
        TitratableAcidityValue.of("6.0")
    );
    return new AppleTargetDistanceView(
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
        AppleTargetDistanceView.ObjectiveFunction.DISTANCE_TO_TARGET_AREA,
        AppleTargetDistanceView.Metric.NORMALIZED_WEIGHTED_EUCLIDEAN,
        AppleTargetDistanceView.TargetKind.CUSTOM_RECTANGLE,
        AppleTargetDistanceView.GeometryShape.RECTANGLE,
        missingAxes
    );
  }
}
