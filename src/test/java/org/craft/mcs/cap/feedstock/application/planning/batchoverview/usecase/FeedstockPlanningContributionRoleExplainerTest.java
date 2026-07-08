package org.craft.mcs.cap.feedstock.application.planning.batchoverview.usecase;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningRoleEvidenceDirection;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningRoleEvidenceEffect;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningRoleEvidenceSeverity;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.usecase.FeedstockPlanningContributionRoleExplainer;
import org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain.CompositionContributionEvidence;
import org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain.CompositionContributionRoleAssessment;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileEvaluationAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain.CompositionContributionRole.TANNIN_SOFTENER;
import static org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain.CompositionContributionRole.UNASSESSABLE;


class FeedstockPlanningContributionRoleExplainerTest {

  private final FeedstockPlanningContributionRoleExplainer explainer = new FeedstockPlanningContributionRoleExplainer();

  @Test
  void returnsTypedEvidenceFactsForContributionRole() {
    final var assessment = new CompositionContributionRoleAssessment(
        List.of(TANNIN_SOFTENER),
        TANNIN_SOFTENER,
        List.of(new CompositionContributionEvidence(
            ProfileEvaluationAxis.TANNIN,
            new BigDecimal("1200"),
            new BigDecimal("1800"),
            new BigDecimal("-600"),
            TANNIN_SOFTENER
        )),
        Set.of()
    );

    final var view = explainer.explain(assessment);

    assertThat(view.evidence())
        .singleElement()
        .satisfies(fact -> assertThat(fact)
            .returns(ProfileEvaluationAxis.TANNIN, evidence -> evidence.axis())
            .returns(FeedstockPlanningRoleEvidenceDirection.BELOW_TARGET_OR_BLEND, evidence -> evidence.direction())
            .returns(FeedstockPlanningRoleEvidenceEffect.SOFTENS_TANNIN_HEAVY_BLEND, evidence -> evidence.effect())
            .returns(FeedstockPlanningRoleEvidenceSeverity.SUPPORTING, evidence -> evidence.severity()));
  }

  @Test
  void keepsUnassessableRoleWithoutInventingTextEvidence() {
    final var assessment = new CompositionContributionRoleAssessment(
        List.of(UNASSESSABLE),
        UNASSESSABLE,
        List.of(),
        Set.of(ChemicalAxis.SUGAR)
    );

    final var view = explainer.explain(assessment);

    assertThat(view)
        .satisfies(viewResult -> assertThat(viewResult.evidence()).isEmpty());
  }
}
