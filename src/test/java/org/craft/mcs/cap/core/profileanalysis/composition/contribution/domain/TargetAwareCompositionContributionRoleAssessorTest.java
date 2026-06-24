package org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileEvaluationAxis;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.CustomRectangleSugarAcidityTarget;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.RectangleSugarAcidityTargetGeometry;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.TanninTargetRange;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class TargetAwareCompositionContributionRoleAssessorTest {

  private final TargetAwareCompositionContributionRoleAssessor assessor =
      new TargetAwareCompositionContributionRoleAssessor();

  @Test
  void marksSourceInsideTargetAsBalancedBase() {
    final var assessment = assessor.assess(request(
        axes("1.055", "5.5", "1200"),
        targetWithTannin()
    ));

    assertThat(assessment.primaryRole()).isEqualTo(CompositionContributionRole.BALANCED_BASE);
    assertThat(assessment.roles()).containsExactly(CompositionContributionRole.BALANCED_BASE);
    assertThat(assessment.evidence()).isEmpty();
    assertThat(assessment.assessable()).isTrue();
  }

  @Test
  void detectsSugarAcidityAndTanninContributorsAboveTarget() {
    final var assessment = assessor.assess(request(
        axes("1.070", "8.5", "2200"),
        targetWithTannin()
    ));

    assertThat(assessment.roles()).containsExactly(
        CompositionContributionRole.SUGAR_CONTRIBUTOR,
        CompositionContributionRole.ACIDITY_CONTRIBUTOR,
        CompositionContributionRole.TANNIN_CONTRIBUTOR
    );
    assertThat(assessment.evidence())
        .extracting(CompositionContributionEvidence::axis)
        .containsExactly(ProfileEvaluationAxis.SUGAR, ProfileEvaluationAxis.ACIDITY, ProfileEvaluationAxis.TANNIN);
  }

  @Test
  void detectsAcidityAndTanninSoftenersBelowTarget() {
    final var assessment = assessor.assess(request(
        axes("1.055", "3.0", "500"),
        targetWithTannin()
    ));

    assertThat(assessment.roles()).containsExactly(
        CompositionContributionRole.ACIDITY_REDUCER,
        CompositionContributionRole.TANNIN_SOFTENER
    );
  }

  @Test
  void marksLowSugarAndLowAciditySourceAsDilutionSource() {
    final var assessment = assessor.assess(request(
        axes("1.040", "3.0", null),
        targetWithoutTannin()
    ));

    assertThat(assessment.roles()).containsExactly(
        CompositionContributionRole.ACIDITY_REDUCER,
        CompositionContributionRole.DILUTION_SOURCE
    );
  }

  @Test
  void reportsUnassessableWhenSugarOrAcidityIsMissing() {
    final var assessment = assessor.assess(request(
        ChemicalProfileAxes.of(axis(ChemicalAxis.SUGAR, "1.055"), null, null),
        targetWithoutTannin()
    ));

    assertThat(assessment.primaryRole()).isEqualTo(CompositionContributionRole.UNASSESSABLE);
    assertThat(assessment.roles()).containsExactly(CompositionContributionRole.UNASSESSABLE);
    assertThat(assessment.missingAxes()).containsExactly(ChemicalAxis.ACIDITY);
    assertThat(assessment.assessable()).isFalse();
  }

  @Test
  void keepsTanninOptionalButReportsMissingWhenTargetRequiresIt() {
    final var assessment = assessor.assess(request(
        axes("1.055", "5.5", null),
        targetWithTannin()
    ));

    assertThat(assessment.primaryRole()).isEqualTo(CompositionContributionRole.LOW_FIT_SOURCE);
    assertThat(assessment.missingAxes()).containsExactly(ChemicalAxis.TANNINITY);
  }

  private static CompositionContributionRoleAssessmentRequest request(
      ChemicalProfileAxes axes,
      ProfileTargetObjective target
  ) {
    return new CompositionContributionRoleAssessmentRequest(axes, target);
  }

  private static ProfileTargetObjective targetWithoutTannin() {
    return ProfileTargetObjective.of(new CustomRectangleSugarAcidityTarget(
        new RectangleSugarAcidityTargetGeometry(
            new BigDecimal("1.050"),
            new BigDecimal("1.060"),
            new BigDecimal("5.0"),
            new BigDecimal("6.0")
        )
    ));
  }

  private static ProfileTargetObjective targetWithTannin() {
    return ProfileTargetObjective.withTanninTarget(
        targetWithoutTannin().sugarAcidityTarget(),
        new TanninTargetRange(new BigDecimal("1000"), new BigDecimal("1500"))
    );
  }

  private static ChemicalProfileAxes axes(
      String sugar,
      String acidity,
      String tannin
  ) {
    return ChemicalProfileAxes.of(
        axis(ChemicalAxis.SUGAR, sugar),
        axis(ChemicalAxis.ACIDITY, acidity),
        tannin == null ? null : axis(ChemicalAxis.TANNINITY, tannin)
    );
  }

  private static AxisValue axis(
      ChemicalAxis axis,
      String value
  ) {
    return new AxisValue(
        axis,
        new BigDecimal(value),
        MeasurementValueKind.EXACT,
        null,
        null,
        null,
        null
    );
  }
}
