package org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.usecase;

import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningRoleEvidenceDirection;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningRoleEvidenceEffect;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningRoleEvidenceFact;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningRoleEvidenceSeverity;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningRoleExplanationView;
import org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain.CompositionContributionEvidence;
import org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain.CompositionContributionRole;
import org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain.CompositionContributionRoleAssessment;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * Converts target-aware contribution-role assessment into typed planning facts.
 *
 * <p>The assessment itself stays in profile-analysis domain. This explainer only prepares a
 * read-side projection for feedstock planning rows, so clients can build localized user-facing
 * explanations without reimplementing CAP role semantics.
 */
@Component
public final class FeedstockPlanningContributionRoleExplainer {

  public @NonNull FeedstockPlanningRoleExplanationView explain(
      @NonNull CompositionContributionRoleAssessment assessment
  ) {
    requireNonNull(assessment, "composition contribution role assessment cannot be null");
    final var evidence = assessment.evidence().stream()
        .map(FeedstockPlanningContributionRoleExplainer::toFact)
        .toList();
    return new FeedstockPlanningRoleExplanationView(evidence);
  }

  private static @NonNull FeedstockPlanningRoleEvidenceFact toFact(
      @NonNull CompositionContributionEvidence evidence
  ) {
    requireNonNull(evidence, "composition contribution evidence cannot be null");
    return new FeedstockPlanningRoleEvidenceFact(
        evidence.axis(),
        direction(evidence),
        effect(evidence.role()),
        severity(evidence.role())
    );
  }

  private static @NonNull FeedstockPlanningRoleEvidenceDirection direction(
      @NonNull CompositionContributionEvidence evidence
  ) {
    requireNonNull(evidence, "composition contribution evidence cannot be null");
    return switch (evidence.deviationFromTarget().signum()) {
      case -1 -> FeedstockPlanningRoleEvidenceDirection.BELOW_TARGET_OR_BLEND;
      case 1 -> FeedstockPlanningRoleEvidenceDirection.ABOVE_TARGET_OR_BLEND;
      default -> FeedstockPlanningRoleEvidenceDirection.ON_TARGET_OR_BLEND;
    };
  }

  private static @NonNull FeedstockPlanningRoleEvidenceEffect effect(
      @NonNull CompositionContributionRole role
  ) {
    requireNonNull(role, "composition contribution role cannot be null");
    return switch (role) {
      case SUGAR_CONTRIBUTOR -> FeedstockPlanningRoleEvidenceEffect.RAISES_SUGAR;
      case ACIDITY_CONTRIBUTOR -> FeedstockPlanningRoleEvidenceEffect.RAISES_ACIDITY;
      case TANNIN_CONTRIBUTOR -> FeedstockPlanningRoleEvidenceEffect.BUILDS_TANNIN_STRUCTURE;
      case ACIDITY_REDUCER -> FeedstockPlanningRoleEvidenceEffect.SOFTENS_ACIDITY_HEAVY_BLEND;
      case TANNIN_SOFTENER -> FeedstockPlanningRoleEvidenceEffect.SOFTENS_TANNIN_HEAVY_BLEND;
      case DILUTION_SOURCE -> FeedstockPlanningRoleEvidenceEffect.DILUTES_SUGAR_AND_ACIDITY;
      case BALANCED_BASE, LOW_FIT_SOURCE, UNASSESSABLE -> FeedstockPlanningRoleEvidenceEffect.NO_CLEAR_EFFECT;
    };
  }

  private static @NonNull FeedstockPlanningRoleEvidenceSeverity severity(
      @NonNull CompositionContributionRole role
  ) {
    requireNonNull(role, "composition contribution role cannot be null");
    return switch (role) {
      case SUGAR_CONTRIBUTOR,
          ACIDITY_CONTRIBUTOR,
          TANNIN_CONTRIBUTOR,
          ACIDITY_REDUCER,
          TANNIN_SOFTENER,
          DILUTION_SOURCE -> FeedstockPlanningRoleEvidenceSeverity.SUPPORTING;
      case BALANCED_BASE -> FeedstockPlanningRoleEvidenceSeverity.NEUTRAL;
      case LOW_FIT_SOURCE, UNASSESSABLE -> FeedstockPlanningRoleEvidenceSeverity.LIMITATION;
    };
  }
}
