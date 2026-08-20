package org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring;

import java.math.BigDecimal;
import java.util.Set;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.assessment.ProfileTargetDistance;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.assessment.ProfileTargetDistanceStatus;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.assessment.ProfileTargetFitAssessment;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.assessment.ProfileTargetFitPolicy;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.assessment.ProfileTargetFitStatus;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.objective.ProfileDistanceMetric;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.objective.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry.SugarAcidityGraphPoint;
import org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry.SugarAcidityGeometryShape;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.target.SugarAcidityTargetKind;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.target.SugarAcidityTargetPosition;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRangePosition;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ProfileTargetFitPolicyTest {

  @ParameterizedTest
  @CsvSource({
      "0.05, 95, EXCELLENT",
      "0.15, 87, CLOSE",
      "0.30, 77, CLOSE",
      "0.44, 69, NEEDS_ADJUSTMENT",
      "1.50, 40, NEEDS_ADJUSTMENT",
      "9.00, 10, NEEDS_ADJUSTMENT"
  })
  void convertsTechnicalDistanceToAuthoritativeFit(
      @NonNull String distance,
      int expectedMatchPercent,
      @NonNull ProfileTargetFitStatus expectedStatus
  ) {
    final var assessment = ProfileTargetFitPolicy.assess(scoredDistance(distance));

    assertThat(assessment)
        .returns(expectedMatchPercent, ProfileTargetFitAssessment::matchPercent)
        .returns(expectedStatus, ProfileTargetFitAssessment::status);
  }

  @Test
  void returnsUnscorableFitWithoutMatchPercent() {
    final var assessment = ProfileTargetFitPolicy.assess(unscorableDistance());

    assertThat(assessment)
        .returns(null, ProfileTargetFitAssessment::matchPercent)
        .returns(ProfileTargetFitStatus.UNSCORABLE, ProfileTargetFitAssessment::status);
  }

  @Test
  void reservesPerfectMatchForActualTargetMembership() {
    final var outsideClosureBoundary = ProfileTargetFitPolicy.assess(scoredDistance("0"));
    final var insideTarget = ProfileTargetFitPolicy.assess(insideDistance());

    assertThat(outsideClosureBoundary)
        .returns(99, ProfileTargetFitAssessment::matchPercent)
        .returns(ProfileTargetFitStatus.EXCELLENT, ProfileTargetFitAssessment::status);
    assertThat(insideTarget)
        .returns(100, ProfileTargetFitAssessment::matchPercent)
        .returns(ProfileTargetFitStatus.EXCELLENT, ProfileTargetFitAssessment::status);
  }

  @Test
  void rejectsStatusThatContradictsMatchPercent() {
    assertThatThrownBy(() -> new ProfileTargetFitAssessment(10, ProfileTargetFitStatus.EXCELLENT))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("profile target fit status does not match its percentage");
  }

  private static @NonNull ProfileTargetDistance scoredDistance(@NonNull String value) {
    return distance(ProfileTargetDistanceStatus.SCORED, new BigDecimal(value), Set.of());
  }

  private static @NonNull ProfileTargetDistance unscorableDistance() {
    return distance(ProfileTargetDistanceStatus.UNSCORABLE, null, Set.of(ProfileEvaluationAxis.SPECIFIC_GRAVITY));
  }

  private static @NonNull ProfileTargetDistance insideDistance() {
    final var point = new SugarAcidityGraphPoint(
        SpecificGravityValue.of("1.050"),
        TitratableAcidityValue.of("6.0")
    );
    return new ProfileTargetDistance(
        ProfileTargetDistanceStatus.SCORED,
        point,
        point,
        new SugarAcidityTargetPosition(
            MeasuredValueRangePosition.WITHIN,
            MeasuredValueRangePosition.WITHIN
        ),
        null,
        null,
        BigDecimal.ZERO,
        true,
        true,
        ProfileTargetObjectiveFunctionCode.DISTANCE_TO_TARGET_AREA,
        ProfileDistanceMetric.NORMALIZED_WEIGHTED_EUCLIDEAN,
        ProfileTargetObjectiveWeights.balanced(),
        SugarAcidityTargetKind.CUSTOM_RECTANGLE,
        SugarAcidityGeometryShape.RECTANGLE,
        Set.of()
    );
  }

  private static @NonNull ProfileTargetDistance distance(
      @NonNull ProfileTargetDistanceStatus status,
      @Nullable BigDecimal value,
      @NonNull Set<@NonNull ProfileEvaluationAxis> missingAxes
  ) {
    final var point = new SugarAcidityGraphPoint(
        SpecificGravityValue.of("1.050"),
        TitratableAcidityValue.of("6.0")
    );
    return new ProfileTargetDistance(
        status,
        point,
        point,
        new SugarAcidityTargetPosition(
            MeasuredValueRangePosition.ABOVE,
            MeasuredValueRangePosition.WITHIN
        ),
        null,
        null,
        value,
        false,
        false,
        ProfileTargetObjectiveFunctionCode.DISTANCE_TO_TARGET_AREA,
        ProfileDistanceMetric.NORMALIZED_WEIGHTED_EUCLIDEAN,
        ProfileTargetObjectiveWeights.balanced(),
        SugarAcidityTargetKind.CUSTOM_RECTANGLE,
        SugarAcidityGeometryShape.RECTANGLE,
        missingAxes
    );
  }
}
