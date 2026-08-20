package org.craft.mcs.cap.appleprofile.analysis.application.profiletarget.port.out;

import java.util.List;
import java.util.Set;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleContributionView;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleTargetCharacteristics;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.port.out.AppleTargetFitAnalyzer;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class AppleTargetFitAnalyzerTest {

  @Test
  void rejectsBlankCandidateReference() {
    assertThatThrownBy(() -> new AppleTargetFitAnalyzer.Candidate(" ", characteristics()))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessageContaining("reference");
  }

  @Test
  void rejectsScoreWithoutDistance() {
    assertThatNullPointerException()
        .isThrownBy(() -> new AppleTargetFitAnalyzer.Score(
            "variety-1",
            null,
            new AppleContributionView(
                List.of(AppleContributionView.Role.BALANCED_BASE),
                List.of(),
                Set.of()
            )
        ));
  }

  private static @NonNull AppleTargetCharacteristics characteristics() {
    return new AppleTargetCharacteristics(
        PointCharacteristicValue.of(SpecificGravityValue.of("1.052")),
        PointCharacteristicValue.of(TitratableAcidityValue.of("6.2")),
        null
    );
  }
}
