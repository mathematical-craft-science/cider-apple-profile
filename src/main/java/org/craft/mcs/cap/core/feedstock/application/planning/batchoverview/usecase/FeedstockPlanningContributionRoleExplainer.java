package org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.usecase;

import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain.CompositionContributionEvidence;
import org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain.CompositionContributionRole;
import org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain.CompositionContributionRoleAssessment;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningRoleExplanationView;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * Converts target-aware contribution-role assessment into compact planning text.
 *
 * <p>The assessment itself stays in profile-analysis domain. This explainer only prepares a
 * read-side summary for feedstock planning rows, so clients can show the reason without
 * reimplementing CAP role semantics.
 */
@Component
public final class FeedstockPlanningContributionRoleExplainer {

  public @NonNull FeedstockPlanningRoleExplanationView explain(
      @NonNull CompositionContributionRoleAssessment assessment
  ) {
    requireNonNull(assessment, "composition contribution role assessment cannot be null");
    if (!assessment.assessable()) {
      return new FeedstockPlanningRoleExplanationView(
          "Role cannot be assessed for the selected target",
          List.of("Missing axes: " + assessment.missingAxes())
      );
    }

    final var reasons = assessment.evidence().stream()
        .map(FeedstockPlanningContributionRoleExplainer::roleReason)
        .toList();
    return new FeedstockPlanningRoleExplanationView(
        "Primary role: " + roleLabel(assessment.primaryRole()),
        reasons
    );
  }

  private static @NonNull String roleReason(@NonNull CompositionContributionEvidence evidence) {
    requireNonNull(evidence, "composition contribution evidence cannot be null");
    return evidence.axis() + " supports " + roleLabel(evidence.role());
  }

  private static @NonNull String roleLabel(@NonNull CompositionContributionRole role) {
    requireNonNull(role, "composition contribution role cannot be null");
    return switch (role) {
      case BALANCED_BASE -> "balanced base";
      case SUGAR_CONTRIBUTOR -> "sugar contributor";
      case ACIDITY_CONTRIBUTOR -> "acidity contributor";
      case TANNIN_CONTRIBUTOR -> "tannin contributor";
      case ACIDITY_REDUCER -> "acidity reducer";
      case TANNIN_SOFTENER -> "tannin softener";
      case DILUTION_SOURCE -> "dilution source";
      case LOW_FIT_SOURCE -> "low-fit source";
      case UNASSESSABLE -> "unassessable source";
    };
  }
}
