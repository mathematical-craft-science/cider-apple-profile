package org.craft.mcs.cap.core.cideranalysis.targetfit.domain.contribution;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.ProfileEvaluationAxis;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRangePosition;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class CompositionContributionEvidenceTest {

  @Test
  void interpretsAboveTargetSpecificGravityAsSugarContribution() {
    final var evidence = evidence(ProfileEvaluationAxis.SPECIFIC_GRAVITY, "1.070", "1.060");

    assertThat(evidence)
        .returns(CompositionContributionEvidenceDirection.ABOVE_TARGET, CompositionContributionEvidence::direction)
        .returns(
            CompositionContributionEvidenceEffect.RAISES_SUGAR_POTENTIAL,
            CompositionContributionEvidence::effect
        )
        .returns(CompositionContributionEvidenceSeverity.SUPPORTING, CompositionContributionEvidence::severity);
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

  @Test
  void permitsAboveTargetPositionWithZeroDeviationAtExcludedUpperBoundary() {
    final var evidence = new CompositionContributionEvidence(
        ProfileEvaluationAxis.SPECIFIC_GRAVITY,
        SpecificGravityValue.of(new BigDecimal("1.045")),
        SpecificGravityValue.of(new BigDecimal("1.045")),
        MeasuredValueRangePosition.ABOVE
    );

    assertThat(evidence)
        .satisfies(value -> assertThat(value.deviationFromTarget())
            .isEqualByComparingTo(BigDecimal.ZERO))
        .returns(
            CompositionContributionEvidenceDirection.ABOVE_TARGET,
            CompositionContributionEvidence::direction
        );
  }

  @Test
  void rejectsTargetPositionThatContradictsNumericDeviation() {
    assertThatThrownBy(() -> new CompositionContributionEvidence(
        ProfileEvaluationAxis.SPECIFIC_GRAVITY,
        SpecificGravityValue.of(new BigDecimal("1.040")),
        SpecificGravityValue.of(new BigDecimal("1.045")),
        MeasuredValueRangePosition.ABOVE
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("composition contribution target position contradicts its numeric deviation");
  }

  @Test
  void rejectsValuesThatDoNotBelongToTheDeclaredAxis() {
    assertThatThrownBy(() -> new CompositionContributionEvidence(
        ProfileEvaluationAxis.SPECIFIC_GRAVITY,
        TanninValue.of(new BigDecimal("800")),
        TanninValue.of(new BigDecimal("1000")),
        MeasuredValueRangePosition.BELOW
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("composition contribution evidence values must match their evaluation axis");
  }

  private static @NonNull CompositionContributionEvidence evidence(
      @NonNull ProfileEvaluationAxis axis,
      @NonNull String source,
      @NonNull String target
  ) {
    return new CompositionContributionEvidence(
        axis,
        measuredValue(axis, source),
        measuredValue(axis, target),
        MeasuredValueRangePosition.fromComparison(
            new BigDecimal(source).compareTo(new BigDecimal(target))
        )
    );
  }

  private static @NonNull MeasuredValue measuredValue(
      @NonNull ProfileEvaluationAxis axis,
      @NonNull String value
  ) {
    final var decimal = new BigDecimal(value);
    return switch (axis) {
      case SPECIFIC_GRAVITY -> SpecificGravityValue.of(decimal);
      case TITRATABLE_ACIDITY -> TitratableAcidityValue.of(decimal);
      case TANNIN -> TanninValue.of(decimal);
    };
  }
}
