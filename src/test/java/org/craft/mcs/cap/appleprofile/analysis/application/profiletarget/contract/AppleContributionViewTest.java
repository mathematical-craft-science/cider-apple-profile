package org.craft.mcs.cap.appleprofile.analysis.application.profiletarget.contract;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleContributionView;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleTargetDistanceView;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class AppleContributionViewTest {

  @Test
  void rejectsMissingContributionRole() {
    assertThatThrownBy(() -> new AppleContributionView(List.of(), List.of(), Set.of()))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessageContaining("role");
  }

  @Test
  void rejectsNullEvidenceAxis() {
    assertThatNullPointerException()
        .isThrownBy(() -> new AppleContributionView.Evidence(
            null,
            BigDecimal.ONE,
            BigDecimal.ONE,
            BigDecimal.ZERO,
            AppleContributionView.Direction.ON_TARGET,
            AppleContributionView.Effect.NO_DIRECTIONAL_EFFECT,
            AppleContributionView.Severity.NEUTRAL
        ));
  }

  @Test
  void rejectsNullEvidenceSourceValue() {
    assertThatNullPointerException()
        .isThrownBy(() -> new AppleContributionView.Evidence(
            AppleTargetDistanceView.EvaluationAxis.SPECIFIC_GRAVITY,
            null,
            BigDecimal.ONE,
            BigDecimal.ZERO,
            AppleContributionView.Direction.ON_TARGET,
            AppleContributionView.Effect.NO_DIRECTIONAL_EFFECT,
            AppleContributionView.Severity.NEUTRAL
        ));
  }
}
