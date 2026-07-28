package org.craft.mcs.cap.feedstock.application.analysis.profiletarget.usecase;

import java.util.List;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockBatchProfileTargetCandidate;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockBatchProfileTargetQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockBatchProfileTargetScore;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockBatchProfileTargetScores;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.PredefinedFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.ScoreFeedstockBatchProfileTargetUseCase;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.usecase.QueryFeedstockBatchProfileTargetCandidatesService;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain.CompositionContributionRoleAssessment;
import org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain.CompositionContributionRoleAssessor;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.model.ProfileTargetAnalysisScenario;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringInput;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment.ProfileTargetDistance;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZone;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class QueryFeedstockBatchProfileTargetCandidatesServiceTest {

  @Test
  void enrichesAuthoritativeScoresWithoutRepeatingTheScoringPipeline() {
    final var scoreUseCase = mock(ScoreFeedstockBatchProfileTargetUseCase.class);
    final var roleAssessor = mock(CompositionContributionRoleAssessor.class);
    final var query = mock(FeedstockBatchProfileTargetQuery.class);
    final var characteristics = mock(ProfileTargetScoringInput.class);
    final var distance = mock(ProfileTargetDistance.class);
    final var roleAssessment = mock(CompositionContributionRoleAssessment.class);
    final var batchId = FeedstockBatchId.newId();
    final var measurementId = FeedstockMeasurementId.newId();
    final var measurementName = FeedstockMeasurementName.of("Current profile");
    final var workspaceId = WorkspaceId.newId();
    final var materialStateId = MaterialStateId.newId();
    final var score = new FeedstockBatchProfileTargetScore(
        batchId,
        workspaceId,
        materialStateId,
        measurementId,
        measurementName,
        characteristics,
        distance
    );
    when(query.analysisScenario()).thenReturn(ProfileTargetAnalysisScenario.BLENDING_PLANNING);
    when(query.target()).thenReturn(
        new PredefinedFeedstockProfileTargetSelection(JolicoeurSugarAcidityZone.PERFECT_APPLES)
    );
    when(scoreUseCase.score(query)).thenReturn(new FeedstockBatchProfileTargetScores(
        ProfileTargetAnalysisScenario.BLENDING_PLANNING,
        ProfileAxisNormalizerCode.EFFECTIVE_RANGE,
        List.of(score)
    ));
    when(roleAssessor.assess(any())).thenReturn(roleAssessment);
    final var service = new QueryFeedstockBatchProfileTargetCandidatesService(
        scoreUseCase,
        roleAssessor
    );

    final var result = service.query(query);

    assertThat(result.candidates())
        .singleElement()
        .returns(batchId, FeedstockBatchProfileTargetCandidate::batchId)
        .returns(materialStateId, FeedstockBatchProfileTargetCandidate::currentMaterialStateId)
        .returns(measurementId, FeedstockBatchProfileTargetCandidate::currentMeasurementId)
        .returns(measurementName, FeedstockBatchProfileTargetCandidate::currentMeasurementName)
        .returns(characteristics, FeedstockBatchProfileTargetCandidate::characteristics)
        .returns(distance, FeedstockBatchProfileTargetCandidate::distance)
        .returns(roleAssessment, FeedstockBatchProfileTargetCandidate::contributionRole);
    verify(scoreUseCase).score(query);
    verify(roleAssessor).assess(argThat(request -> request.targetDistance() == distance));
  }
}
