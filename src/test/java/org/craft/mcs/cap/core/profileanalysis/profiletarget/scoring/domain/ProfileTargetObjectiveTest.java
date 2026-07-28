package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityPointDistance;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityGraphPoint;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.CustomRectangleSugarAcidityTarget;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.RectangleSugarAcidityTargetGeometry;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.TanninTargetRange;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange.closed;


class ProfileTargetObjectiveTest {

  private static final SugarAcidityPointDistance UNUSED_DISTANCE =
      (source, targetPoint) -> BigDecimal.ZERO;

  private final CustomRectangleSugarAcidityTarget target = new CustomRectangleSugarAcidityTarget(
      new RectangleSugarAcidityTargetGeometry(
          closed(SpecificGravityValue.of("1.050"), SpecificGravityValue.of("1.060")),
          closed(TitratableAcidityValue.of("5.0"), TitratableAcidityValue.of("6.0"))
      )
  );

  @Test
  void comparisonKeepsOnlyCharacteristicsAssessedByTarget() {
    final var comparison = ProfileTargetObjective.of(target)
        .compare(point("1.055", "5.5", "1800"), UNUSED_DISTANCE);

    assertThat(comparison)
        .returns(true, ProfileTargetComparison::comparable)
        .returns(true, ProfileTargetComparison::insideTarget)
        .returns(null, ProfileTargetComparison::tannin)
        .returns(null, ProfileTargetComparison::nearestTargetTannin);
  }

  @Test
  void comparisonReportsMissingTanninRequiredByTarget() {
    final var comparison = ProfileTargetObjective.withTanninTarget(
        target,
        new TanninTargetRange(TanninValue.of("1000"), TanninValue.of("1500"))
    ).compare(point("1.055", "5.5", null), UNUSED_DISTANCE);

    assertThat(comparison)
        .returns(false, ProfileTargetComparison::comparable)
        .returns(false, ProfileTargetComparison::insideTarget);
    assertThat(comparison.missingAxes()).containsExactly(ProfileEvaluationAxis.TANNIN);
  }

  private static @NonNull ProfileTargetScoringPoint point(
      @NonNull String specificGravity,
      @NonNull String titratableAcidity,
      @Nullable String tannin
  ) {
    return new ProfileTargetScoringPoint(
        new SugarAcidityGraphPoint(
            SpecificGravityValue.of(specificGravity),
            TitratableAcidityValue.of(titratableAcidity)
        ),
        tannin == null ? null : TanninValue.of(tannin)
    );
  }
}
