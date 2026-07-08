package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.point.domain.SugarAcidityAxisPoint;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.point.domain.SugarAcidityProfilePoint;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.CustomRectangleSugarAcidityTarget;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.RectangleSugarAcidityTargetGeometry;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.SugarAcidityCoordinates;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.SugarAcidityTargetGeometryShape;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.SugarAcidityTargetKind;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.TanninTargetRange;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;


class DistanceToTargetAreaObjectiveFunctionTest {

  private final CustomRectangleSugarAcidityTarget target = new CustomRectangleSugarAcidityTarget(
      new RectangleSugarAcidityTargetGeometry(
          new BigDecimal("1.060"),
          new BigDecimal("1.080"),
          new BigDecimal("4.5"),
          new BigDecimal("7.5")
      )
  );
  private final DistanceToTargetAreaObjectiveFunction objective = new DistanceToTargetAreaObjectiveFunction(
      MinMaxProfileAxisNormalizer.fullCiderProfileRange()
  );

  @Test
  void returnsZeroDistanceInsideTargetArea() {
    final var distance = objective.distanceTo(new SugarAcidityCoordinates(
        new BigDecimal("1.070"),
        new BigDecimal("6.0")
    ), target);

    assertThat(distance.value()).isEqualByComparingTo("0");
    assertThat(distance.status()).isEqualTo(ProfileTargetDistanceStatus.SCORED);
    assertThat(distance.scored()).isTrue();
    assertThat(distance.insideSugarAcidityTarget()).isTrue();
    assertThat(distance.insideTarget()).isTrue();
    assertThat(distance.targetKind()).isEqualTo(SugarAcidityTargetKind.CUSTOM_RECTANGLE);
    assertThat(distance.targetShape()).isEqualTo(SugarAcidityTargetGeometryShape.RECTANGLE);
    assertThat(distance.metric()).isEqualTo(ProfileDistanceMetric.NORMALIZED_WEIGHTED_EUCLIDEAN);
  }

  @Test
  void measuresNormalizedWeightedDistanceToNearestTargetBoundary() {
    final var distance = objective.distanceTo(new SugarAcidityCoordinates(
        new BigDecimal("1.050"),
        new BigDecimal("8.5")
    ), target);

    assertThat(distance.insideTarget()).isFalse();
    assertThat(distance.nearestTargetPoint().sugar()).isEqualByComparingTo("1.060");
    assertThat(distance.nearestTargetPoint().acidity()).isEqualByComparingTo("7.5");
    assertThat(distance.value()).isCloseTo(new BigDecimal("0.194365"), within(new BigDecimal("0.000001")));
  }

  @Test
  void manhattanObjectiveMeasuresL1DistanceToNearestTargetBoundary() {
    final var manhattanObjective = new ManhattanDistanceToTargetAreaObjectiveFunction(
        MinMaxProfileAxisNormalizer.fullCiderProfileRange(),
        ProfileTargetObjectiveWeights.balanced()
    );

    final var distance = manhattanObjective.distanceTo(new SugarAcidityCoordinates(
        new BigDecimal("1.050"),
        new BigDecimal("8.5")
    ), target);

    assertThat(distance.metric()).isEqualTo(ProfileDistanceMetric.NORMALIZED_WEIGHTED_MANHATTAN);
    assertThat(distance.objectiveFunction()).isEqualTo(ProfileTargetObjectiveFunctionCode.MANHATTAN_DISTANCE_TO_TARGET_AREA);
    assertThat(distance.value()).isCloseTo(new BigDecimal("0.266667"), within(new BigDecimal("0.000001")));
  }

  @Test
  void appliesCustomAxisWeights() {
    final var weightedObjective = new DistanceToTargetAreaObjectiveFunction(
        MinMaxProfileAxisNormalizer.fullCiderProfileRange(),
        new ProfileTargetObjectiveWeights(new BigDecimal("4"), BigDecimal.ONE)
    );

    final var distance = weightedObjective.distanceTo(new SugarAcidityCoordinates(
        new BigDecimal("1.050"),
        new BigDecimal("6.0")
    ), target);

    assertThat(distance.value()).isCloseTo(new BigDecimal("0.333333"), within(new BigDecimal("0.000001")));
    assertThat(distance.weights().sugar()).isEqualByComparingTo("4");
    assertThat(distance.weights().acidity()).isEqualByComparingTo("1");
  }

  @Test
  void includesOptionalTanninDistanceWhenTanninTargetIsConfigured() {
    final var objectiveWithTannin = ProfileTargetObjective.withTanninTarget(
        target,
        new TanninTargetRange(new BigDecimal("1500"), new BigDecimal("2500"))
    );

    final var distance = objective.distanceTo(
        point("1.070", "6.0", "1000"),
        objectiveWithTannin
    );

    assertThat(distance.status()).isEqualTo(ProfileTargetDistanceStatus.SCORED);
    assertThat(distance.insideSugarAcidityTarget()).isTrue();
    assertThat(distance.insideTarget()).isFalse();
    assertThat(distance.tannin()).isEqualByComparingTo("1000");
    assertThat(distance.nearestTargetTannin()).isEqualByComparingTo("1500");
    assertThat(distance.value()).isCloseTo(new BigDecimal("0.041666"), within(new BigDecimal("0.000001")));
  }

  @Test
  void returnsUnscorableWhenTanninTargetIsConfiguredButPointHasNoTannin() {
    final var objectiveWithTannin = ProfileTargetObjective.withTanninTarget(
        target,
        new TanninTargetRange(new BigDecimal("1500"), new BigDecimal("2500"))
    );

    final var distance = objective.distanceTo(
        new SugarAcidityProfilePoint(axis("1.070"), axis("6.0"), null),
        objectiveWithTannin
    );

    assertThat(distance.status()).isEqualTo(ProfileTargetDistanceStatus.UNSCORABLE);
    assertThat(distance.scored()).isFalse();
    assertThat(distance.value()).isNull();
    assertThat(distance.missingMeasurements()).containsExactly(ProfileEvaluationAxis.TANNIN);
  }

  @NonNull
  private static SugarAcidityProfilePoint point(
      @NonNull String sugar,
      @NonNull String acidity,
      @NonNull String tannin
  ) {
    return new SugarAcidityProfilePoint(axis(sugar), axis(acidity), axis(tannin));
  }

  @NonNull
  private static SugarAcidityAxisPoint axis(@NonNull String value) {
    return new SugarAcidityAxisPoint(
        new BigDecimal(value),
        MeasurementValueKind.EXACT,
        null,
        null,
        null,
        null
    );
  }
}
