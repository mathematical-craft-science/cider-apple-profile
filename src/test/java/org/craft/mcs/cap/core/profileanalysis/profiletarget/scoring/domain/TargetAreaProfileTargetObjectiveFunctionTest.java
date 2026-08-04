package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.EllipseSugarAcidityGeometry;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment.ProfileTargetDistance;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment.ProfileTargetFitAssessment;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment.ProfileTargetDistanceStatus;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.MinMaxProfileAxisNormalizer;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.ProfileDistanceMetric;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.TargetAreaProfileTargetObjectiveFunction;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.CustomRectangleSugarAcidityTarget;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.EllipseSugarAcidityTargetGeometry;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.PredefinedJolicoeurSugarAcidityZoneTarget;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.RectangleSugarAcidityTargetGeometry;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityGraphPoint;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityGeometryShape;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZone;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.SugarAcidityTargetKind;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.SugarAcidityTargetPosition;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.TanninTargetRange;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRangePosition;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange.closed;


class TargetAreaProfileTargetObjectiveFunctionTest {

  private final CustomRectangleSugarAcidityTarget target =
      new CustomRectangleSugarAcidityTarget(
          new RectangleSugarAcidityTargetGeometry(
              closed(
                  SpecificGravityValue.of("1.060"),
                  SpecificGravityValue.of("1.080")
              ),
              closed(
                  TitratableAcidityValue.of("4.5"),
                  TitratableAcidityValue.of("7.5")
              )
          )
      );
  private final TargetAreaProfileTargetObjectiveFunction objective =
      TargetAreaProfileTargetObjectiveFunction.euclidean(
      MinMaxProfileAxisNormalizer.fullCiderProfileRange()
  );

  @Test
  void returnsZeroDistanceInsideTargetArea() {
    final var distance = objective.distanceTo(
        point("1.070", "6.0", null),
        ProfileTargetIntervalPolicy.MIDPOINT,
        ProfileTargetObjective.of(target)
    );

    assertThat(distance.value()).isEqualByComparingTo("0");
    assertThat(distance.status()).isEqualTo(ProfileTargetDistanceStatus.SCORED);
    assertThat(distance.scored()).isTrue();
    assertThat(distance.insideSugarAcidityTarget()).isTrue();
    assertThat(distance.insideTarget()).isTrue();
    assertThat(distance.targetKind()).isEqualTo(SugarAcidityTargetKind.CUSTOM_RECTANGLE);
    assertThat(distance.targetShape()).isEqualTo(SugarAcidityGeometryShape.RECTANGLE);
    assertThat(distance.metric()).isEqualTo(ProfileDistanceMetric.NORMALIZED_WEIGHTED_EUCLIDEAN);
  }

  @Test
  void ignoresTanninWhenTargetDoesNotAssessIt() {
    final var distance = objective.distanceTo(
        point("1.070", "6.0", "1800"),
        ProfileTargetIntervalPolicy.MIDPOINT,
        ProfileTargetObjective.of(target)
    );

    assertThat(distance)
        .returns(ProfileTargetDistanceStatus.SCORED, ProfileTargetDistance::status)
        .returns(true, ProfileTargetDistance::insideTarget)
        .returns(null, ProfileTargetDistance::tannin)
        .returns(null, ProfileTargetDistance::nearestTargetTannin);
  }

  @Test
  void measuresNormalizedWeightedDistanceToNearestTargetBoundary() {
    final var distance = objective.distanceTo(
        point("1.050", "8.5", null),
        ProfileTargetIntervalPolicy.MIDPOINT,
        ProfileTargetObjective.of(target)
    );

    assertThat(distance.insideTarget()).isFalse();
    assertThat(distance.nearestTargetPoint())
        .returns(
            SpecificGravityValue.of("1.060"),
            SugarAcidityGraphPoint::specificGravity
        )
        .returns(
            TitratableAcidityValue.of("7.5"),
            SugarAcidityGraphPoint::titratableAcidity
        );
    assertThat(distance.value()).isCloseTo(new BigDecimal("0.194365"), within(new BigDecimal("0.000001")));
  }

  @Test
  void manhattanObjectiveMeasuresL1DistanceToNearestTargetBoundary() {
    final var manhattanObjective = TargetAreaProfileTargetObjectiveFunction.manhattan(
        MinMaxProfileAxisNormalizer.fullCiderProfileRange(),
        ProfileTargetObjectiveWeights.balanced()
    );

    final var distance = manhattanObjective.distanceTo(
        point("1.050", "8.5", null),
        ProfileTargetIntervalPolicy.MIDPOINT,
        ProfileTargetObjective.of(target)
    );

    assertThat(distance.metric()).isEqualTo(ProfileDistanceMetric.NORMALIZED_WEIGHTED_MANHATTAN);
    assertThat(distance.objectiveFunction())
        .isEqualTo(ProfileTargetObjectiveFunctionCode.MANHATTAN_DISTANCE_TO_TARGET_AREA);
    assertThat(distance.value()).isCloseTo(new BigDecimal("0.266667"), within(new BigDecimal("0.000001")));
  }

  @Test
  void appliesCustomAxisWeights() {
    final var weightedObjective = TargetAreaProfileTargetObjectiveFunction.euclidean(
        MinMaxProfileAxisNormalizer.fullCiderProfileRange(),
        new ProfileTargetObjectiveWeights(new BigDecimal("4"), BigDecimal.ONE)
    );

    final var distance = weightedObjective.distanceTo(
        point("1.050", "6.0", null),
        ProfileTargetIntervalPolicy.MIDPOINT,
        ProfileTargetObjective.of(target)
    );

    assertThat(distance.value()).isCloseTo(new BigDecimal("0.333333"), within(new BigDecimal("0.000001")));
    assertThat(distance.weights())
        .returns(new BigDecimal("4"), ProfileTargetObjectiveWeights::specificGravity)
        .returns(BigDecimal.ONE, ProfileTargetObjectiveWeights::titratableAcidity);
  }

  @Test
  void ellipseProjectionUsesTheConfiguredAxisWeights() {
    final var weightedObjective = TargetAreaProfileTargetObjectiveFunction.euclidean(
        MinMaxProfileAxisNormalizer.fullCiderProfileRange(),
        new ProfileTargetObjectiveWeights(new BigDecimal("1000"), BigDecimal.ONE)
    );
    final var ellipseTarget = new PredefinedJolicoeurSugarAcidityZoneTarget(
        JolicoeurSugarAcidityZone.PERFECT_APPLES,
        new EllipseSugarAcidityTargetGeometry(
            new EllipseSugarAcidityGeometry(
                SpecificGravityValue.of("1.060"),
                TitratableAcidityValue.of("6.0"),
                new BigDecimal("0.010"),
                new BigDecimal("2.0")
            )
        )
    );

    final var distance = weightedObjective.distanceTo(
        point("1.080", "8.0", null),
        ProfileTargetIntervalPolicy.MIDPOINT,
        ProfileTargetObjective.of(ellipseTarget)
    );

    assertThat(distance.nearestTargetPoint().specificGravity().asBigDecimal())
        .isGreaterThan(new BigDecimal("1.068944"));
    assertThat(distance.nearestTargetPoint().titratableAcidity().asBigDecimal())
        .isLessThan(new BigDecimal("6.8945"));
    assertThat(distance.targetShape()).isEqualTo(SugarAcidityGeometryShape.ELLIPSE);
  }

  @Test
  void treatsClosedEllipseBoundaryAsActualTargetMembership() {
    final var ellipseTarget = new PredefinedJolicoeurSugarAcidityZoneTarget(
        JolicoeurSugarAcidityZone.PERFECT_APPLES,
        new EllipseSugarAcidityTargetGeometry(
            new EllipseSugarAcidityGeometry(
                SpecificGravityValue.of("1.060"),
                TitratableAcidityValue.of("6.0"),
                new BigDecimal("0.010"),
                new BigDecimal("2.0")
            )
        )
    );

    final var distance = objective.distanceTo(
        point("1.070", "6.0", null),
        ProfileTargetIntervalPolicy.MIDPOINT,
        ProfileTargetObjective.of(ellipseTarget)
    );

    assertThat(distance)
        .returns(true, ProfileTargetDistance::insideSugarAcidityTarget)
        .returns(true, ProfileTargetDistance::insideTarget)
        .satisfies(result -> assertThat(result.value()).isEqualByComparingTo(BigDecimal.ZERO))
        .satisfies(result -> assertThat(result.sugarAcidityTargetPosition())
            .returns(
                MeasuredValueRangePosition.WITHIN,
                SugarAcidityTargetPosition::specificGravity
            )
            .returns(
                MeasuredValueRangePosition.WITHIN,
                SugarAcidityTargetPosition::titratableAcidity
            ))
        .satisfies(result -> assertThat(result.fitAssessment())
            .returns(100, ProfileTargetFitAssessment::matchPercent));
  }

  @Test
  void includesOptionalTanninDistanceWhenTanninTargetIsConfigured() {
    final var objectiveWithTannin = ProfileTargetObjective.withTanninTarget(
        target,
        new TanninTargetRange(TanninValue.of("1500"), TanninValue.of("2500"))
    );

    final var distance = objective.distanceTo(
        point("1.070", "6.0", "1000"),
        ProfileTargetIntervalPolicy.MIDPOINT,
        objectiveWithTannin
    );

    assertThat(distance.status()).isEqualTo(ProfileTargetDistanceStatus.SCORED);
    assertThat(distance.insideSugarAcidityTarget()).isTrue();
    assertThat(distance.insideTarget()).isFalse();
    assertThat(distance.tannin())
        .isEqualTo(TanninValue.of("1000"));
    assertThat(distance.nearestTargetTannin())
        .isEqualTo(TanninValue.of("1500"));
    assertThat(distance.value()).isCloseTo(new BigDecimal("0.041666"), within(new BigDecimal("0.000001")));
  }

  @Test
  void returnsUnscorableWhenTanninTargetIsConfiguredButPointHasNoTannin() {
    final var objectiveWithTannin = ProfileTargetObjective.withTanninTarget(
        target,
        new TanninTargetRange(TanninValue.of("1500"), TanninValue.of("2500"))
    );

    final var distance = objective.distanceTo(
        new ProfileTargetScoringInput(
            PointCharacteristicValue.of(SpecificGravityValue.of("1.070")),
            PointCharacteristicValue.of(TitratableAcidityValue.of("6.0")),
            null
        ),
        ProfileTargetIntervalPolicy.MIDPOINT,
        objectiveWithTannin
    );

    assertThat(distance.status()).isEqualTo(ProfileTargetDistanceStatus.UNSCORABLE);
    assertThat(distance.scored()).isFalse();
    assertThat(distance.value()).isNull();
    assertThat(distance.missingAxes()).containsExactly(ProfileEvaluationAxis.TANNIN);
  }

  @NonNull
  private static ProfileTargetScoringInput point(
      @NonNull String specificGravity,
      @NonNull String titratableAcidity,
      @Nullable String tannin
  ) {
    return new ProfileTargetScoringInput(
        PointCharacteristicValue.of(SpecificGravityValue.of(specificGravity)),
        PointCharacteristicValue.of(TitratableAcidityValue.of(titratableAcidity)),
        tannin == null ? null : PointCharacteristicValue.of(TanninValue.of(tannin))
    );
  }
}
