package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain;

import java.math.BigDecimal;
import java.util.Set;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment.ProfileTargetDistance;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment.ProfileTargetDistanceStatus;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment.ProfileTargetFitAssessment;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment.ProfileTargetFitPolicy;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment.ProfileTargetFitStatus;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.ProfileDistanceMetric;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityGraphPoint;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityGeometryShape;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.SugarAcidityTargetKind;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
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
