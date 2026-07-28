package org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange.closed;

import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileEvaluationAxis;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetIntervalPolicy;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringInput;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisSensitivityUnits;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.SensitivityUnitProfileAxisNormalizer;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.TargetAreaProfileTargetObjectiveFunction;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.CustomRectangleSugarAcidityTarget;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.RectangleSugarAcidityTargetGeometry;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.TanninTargetRange;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;


class TargetAwareCompositionContributionRoleAssessorTest {

  private final TargetAwareCompositionContributionRoleAssessor assessor =
      new TargetAwareCompositionContributionRoleAssessor();

  @Test
  void marksSourceInsideTargetAsBalancedBase() {
    final var assessment = assessor.assess(request(
        input("1.055", "5.5", "1200"),
        targetWithTannin()
    ));

    assertThat(assessment)
        .returns(CompositionContributionRole.BALANCED_BASE, CompositionContributionRoleAssessment::primaryRole);
    assertThat(assessment.roles()).containsExactly(CompositionContributionRole.BALANCED_BASE);
    assertThat(assessment.evidence())
        .hasSize(3)
        .allSatisfy(evidence -> assertThat(evidence)
            .returns(
                CompositionContributionEvidenceDirection.ON_TARGET,
                CompositionContributionEvidence::direction
            )
            .returns(
                CompositionContributionEvidenceSeverity.NEUTRAL,
                CompositionContributionEvidence::severity
            ));
  }

  @Test
  void detectsSugarAcidityAndTanninContributionRolesAboveTarget() {
    final var assessment = assessor.assess(request(
        input("1.070", "8.5", "2200"),
        targetWithTannin()
    ));

    assertThat(assessment.roles()).containsExactly(
        CompositionContributionRole.SUGAR_CONTRIBUTOR,
        CompositionContributionRole.ACIDITY_CONTRIBUTOR,
        CompositionContributionRole.TANNIN_CONTRIBUTOR
    );
    assertThat(assessment.evidence())
        .extracting(CompositionContributionEvidence::axis)
        .containsExactly(
            ProfileEvaluationAxis.SPECIFIC_GRAVITY,
            ProfileEvaluationAxis.TITRATABLE_ACIDITY,
            ProfileEvaluationAxis.TANNIN
        );
  }

  @Test
  void detectsTitratableAcidityAndTanninSoftenersBelowTarget() {
    final var assessment = assessor.assess(request(
        input("1.055", "3.0", "500"),
        targetWithTannin()
    ));

    assertThat(assessment.roles()).containsExactly(
        CompositionContributionRole.ACIDITY_REDUCER,
        CompositionContributionRole.TANNIN_SOFTENER
    );
  }

  @Test
  void marksLowSpecificGravityAndTitratableAciditySourceAsDilutionSource() {
    final var assessment = assessor.assess(request(
        input("1.040", "3.0", null),
        targetWithoutTannin()
    ));

    assertThat(assessment.roles()).containsExactly(
        CompositionContributionRole.SUGAR_REDUCER,
        CompositionContributionRole.ACIDITY_REDUCER,
        CompositionContributionRole.DILUTION_SOURCE
    );
  }

  @Test
  void keepsTanninOptionalButReportsMissingWhenTargetRequiresIt() {
    final var assessment = assessor.assess(request(
        input("1.055", "5.5", null),
        targetWithTannin()
    ));

    assertThat(assessment)
        .returns(CompositionContributionRole.BALANCED_BASE, CompositionContributionRoleAssessment::primaryRole);
    assertThat(assessment.missingAxes()).containsExactly(ProfileEvaluationAxis.TANNIN);
  }

  private static @NonNull CompositionContributionRoleAssessmentRequest request(
      @NonNull ProfileTargetScoringInput input,
      @NonNull ProfileTargetObjective target
  ) {
    final var objectiveFunction = TargetAreaProfileTargetObjectiveFunction.euclidean(
        new SensitivityUnitProfileAxisNormalizer(
            ProfileAxisSensitivityUnits.ciderBlendingV1()
        )
    );
    return new CompositionContributionRoleAssessmentRequest(
        objectiveFunction.distanceTo(input, ProfileTargetIntervalPolicy.MIDPOINT, target)
    );
  }

  private static @NonNull ProfileTargetObjective targetWithoutTannin() {
    return ProfileTargetObjective.of(new CustomRectangleSugarAcidityTarget(
        new RectangleSugarAcidityTargetGeometry(
            closed(
                SpecificGravityValue.of("1.050"),
                SpecificGravityValue.of("1.060")
            ),
            closed(
                TitratableAcidityValue.of("5.0"),
                TitratableAcidityValue.of("6.0")
            )
        )
    ));
  }

  private static @NonNull ProfileTargetObjective targetWithTannin() {
    return ProfileTargetObjective.withTanninTarget(
        targetWithoutTannin().sugarAcidityTarget(),
        new TanninTargetRange(TanninValue.of("1000"), TanninValue.of("1500"))
    );
  }

  private static @NonNull ProfileTargetScoringInput input(
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
