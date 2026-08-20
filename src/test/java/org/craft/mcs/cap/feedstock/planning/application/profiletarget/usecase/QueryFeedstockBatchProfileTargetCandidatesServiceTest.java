package org.craft.mcs.cap.feedstock.planning.application.profiletarget.usecase;

import java.util.List;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockContributionView;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetCharacteristics;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetDistanceView;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration.AnalysisScenario;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration.Normalizer;
import org.craft.mcs.cap.core.feedstock.planning.application.graph.contract.FeedstockPlanningGraphView.Zone;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in.FeedstockBatchProfileTargetCandidate;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in.FeedstockBatchProfileTargetQuery;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in.FeedstockBatchProfileTargetScore;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in.FeedstockBatchProfileTargetScores;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.PredefinedFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in.ScoreFeedstockBatchProfileTargetUseCase;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.usecase.QueryFeedstockBatchProfileTargetCandidatesService;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceView;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class QueryFeedstockBatchProfileTargetCandidatesServiceTest {

  @Test
  void enrichesAuthoritativeScoresWithoutRepeatingTheScoringPipeline() {
    final var scoreUseCase = mock(ScoreFeedstockBatchProfileTargetUseCase.class);
    final var query = mock(FeedstockBatchProfileTargetQuery.class);
    final var characteristics = mock(FeedstockTargetCharacteristics.class);
    final var distance = mock(FeedstockTargetDistanceView.class);
    final var contribution = mock(FeedstockContributionView.class);
    final var batchId = FeedstockBatchId.newId();
    final var profileEvidence = List.of(mock(MeasurementEvidenceView.class));
    final var workspaceId = WorkspaceId.newId();
    final var materialStateId = MaterialStateId.newId();
    final var score = new FeedstockBatchProfileTargetScore(
        batchId,
        workspaceId,
        materialStateId,
        profileEvidence,
        characteristics,
        distance,
        contribution
    );
    when(query.analysisScenario()).thenReturn(AnalysisScenario.BLENDING_PLANNING);
    when(query.target()).thenReturn(
        new PredefinedFeedstockProfileTargetSelection(Zone.PERFECT_APPLES)
    );
    when(scoreUseCase.score(query)).thenReturn(new FeedstockBatchProfileTargetScores(
        AnalysisScenario.BLENDING_PLANNING,
        Normalizer.EFFECTIVE_RANGE,
        List.of(score)
    ));
    final var service = new QueryFeedstockBatchProfileTargetCandidatesService(scoreUseCase);

    final var result = service.query(query);

    assertThat(result.candidates())
        .singleElement()
        .returns(batchId, FeedstockBatchProfileTargetCandidate::batchId)
        .returns(materialStateId, FeedstockBatchProfileTargetCandidate::currentMaterialStateId)
        .returns(profileEvidence, FeedstockBatchProfileTargetCandidate::profileEvidence)
        .returns(characteristics, FeedstockBatchProfileTargetCandidate::characteristics)
        .returns(distance, FeedstockBatchProfileTargetCandidate::distance)
        .returns(contribution, FeedstockBatchProfileTargetCandidate::contributionRole);
    verify(scoreUseCase).score(query);
  }

}
