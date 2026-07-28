package org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileEvaluationAxis;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class CompositionContributionEvidenceTest {

  @Test
  void interpretsAboveTargetSpecificGravityAsSugarContribution() {
    final var evidence = evidence(ProfileEvaluationAxis.SPECIFIC_GRAVITY, "1.070", "1.060");

    assertThat(evidence)
        .returns(CompositionContributionEvidenceDirection.ABOVE_TARGET, CompositionContributionEvidence::direction)
        .returns(
            CompositionContributionEvidenceEffect.RAISES_SUGAR_POTENTIAL,
            CompositionContributionEvidence::effect
        );
    assertThat(evidence.severity())
        .isEqualTo(CompositionContributionEvidenceSeverity.SUPPORTING);
  }

  @Test
  void interpretsBelowTargetTanninAsSofteningContribution() {
    final var evidence = evidence(ProfileEvaluationAxis.TANNIN, "800", "1000");

    assertThat(evidence)
        .returns(CompositionContributionEvidenceDirection.BELOW_TARGET, CompositionContributionEvidence::direction)
        .returns(
            CompositionContributionEvidenceEffect.SOFTENS_TANNIN_HEAVY_BLEND,
            CompositionContributionEvidence::effect
        );
  }

  @Test
  void interpretsBelowTargetSpecificGravityAsSupportingSugarReduction() {
    final var evidence = evidence(ProfileEvaluationAxis.SPECIFIC_GRAVITY, "1.040", "1.050");

    assertThat(evidence.severity())
        .isEqualTo(CompositionContributionEvidenceSeverity.SUPPORTING);
  }

  private static @NonNull CompositionContributionEvidence evidence(
      @NonNull ProfileEvaluationAxis axis,
      @NonNull String source,
      @NonNull String target
  ) {
    return new CompositionContributionEvidence(
        axis,
        new BigDecimal(source),
        new BigDecimal(target)
    );
  }
}
