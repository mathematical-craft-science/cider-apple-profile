package org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileEvaluationAxis;
import org.jspecify.annotations.NonNull;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain.CompositionContributionRole.ACIDITY_REDUCER;
import static org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain.CompositionContributionRole.TANNIN_SOFTENER;


class CompositionContributionRoleAssessmentTest {

  @Test
  void derivesPrimaryRoleFromCanonicalDomainOrder() {
    final var assessment = new CompositionContributionRoleAssessment(
        List.of(TANNIN_SOFTENER, ACIDITY_REDUCER),
        List.of(),
        Set.of()
    );

    assertThat(assessment)
        .returns(ACIDITY_REDUCER, CompositionContributionRoleAssessment::primaryRole)
        .extracting(CompositionContributionRoleAssessment::roles)
        .isEqualTo(List.of(ACIDITY_REDUCER, TANNIN_SOFTENER));
  }

  @Test
  void rejectsEmptyRoles() {
    assertThatThrownBy(() -> new CompositionContributionRoleAssessment(
        List.of(),
        List.of(),
        Set.of()
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("composition contribution assessment requires at least one role");
  }

  @Test
  void rejectsDuplicateRoles() {
    assertThatThrownBy(() -> new CompositionContributionRoleAssessment(
        List.of(TANNIN_SOFTENER, TANNIN_SOFTENER),
        List.of(),
        Set.of()
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("composition contribution roles must be unique");
  }

  @Test
  void rejectsDuplicateEvidenceForTheSameAxis() {
    final var evidence = evidence(ProfileEvaluationAxis.SPECIFIC_GRAVITY);

    assertThatThrownBy(() -> new CompositionContributionRoleAssessment(
        List.of(ACIDITY_REDUCER),
        List.of(evidence, evidence),
        Set.of()
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("composition contribution evidence axes must be unique");
  }

  @Test
  void rejectsEvidenceForAnAxisDeclaredMissing() {
    final var evidence = evidence(ProfileEvaluationAxis.TANNIN);

    assertThatThrownBy(() -> new CompositionContributionRoleAssessment(
        List.of(TANNIN_SOFTENER),
        List.of(evidence),
        Set.of(ProfileEvaluationAxis.TANNIN)
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("missing composition contribution axes cannot also have evidence");
  }

  private static @NonNull CompositionContributionEvidence evidence(
      @NonNull ProfileEvaluationAxis axis
  ) {
    return new CompositionContributionEvidence(
        axis,
        BigDecimal.ONE,
        BigDecimal.TEN
    );
  }
}
