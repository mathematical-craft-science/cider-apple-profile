package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment;

import java.math.BigDecimal;
import java.util.Set;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.ProfileDistanceMetric;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityGraphPoint;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityGeometryShape;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.SugarAcidityTargetKind;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ProfileTargetDistanceTest {

  @Test
  void rejectsFullTargetMatchOutsideSugarAcidityTarget() {
    assertThatThrownBy(() -> distance(false, true, BigDecimal.ZERO))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("profile cannot be inside the full target outside its SG/TA target");
  }

  @Test
  void rejectsNonZeroDistanceInsideTarget() {
    assertThatThrownBy(() -> distance(true, true, BigDecimal.ONE))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("profile inside the target must have zero distance");
  }

  private static ProfileTargetDistance distance(
      boolean insideSugarAcidityTarget,
      boolean insideTarget,
      BigDecimal value
  ) {
    final var point = new SugarAcidityGraphPoint(
        SpecificGravityValue.of("1.060"),
        TitratableAcidityValue.of("6.0")
    );
    return new ProfileTargetDistance(
        ProfileTargetDistanceStatus.SCORED,
        point,
        point,
        null,
        null,
        value,
        insideSugarAcidityTarget,
        insideTarget,
        ProfileTargetObjectiveFunctionCode.DISTANCE_TO_TARGET_AREA,
        ProfileDistanceMetric.NORMALIZED_WEIGHTED_EUCLIDEAN,
        ProfileTargetObjectiveWeights.balanced(),
        SugarAcidityTargetKind.CUSTOM_RECTANGLE,
        SugarAcidityGeometryShape.RECTANGLE,
        Set.of()
    );
  }
}
