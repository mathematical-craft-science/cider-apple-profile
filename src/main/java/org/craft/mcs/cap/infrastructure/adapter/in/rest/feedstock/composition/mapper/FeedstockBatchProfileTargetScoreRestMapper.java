package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.mapper;

import org.craft.mcs.cap.core.feedstock.planning.application.candidate.contract.FeedstockPlanningProfileFilters;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in.FeedstockBatchProfileTargetQuery;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in.FeedstockBatchProfileTargetScore;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in.FeedstockBatchProfileTargetScores;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper.FeedstockProfileTargetRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.dto.FeedstockBatchProfileTargetScoreBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.dto.FeedstockBatchProfileTargetScoreResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.dto.ScoreFeedstockBatchProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper.ProfileTargetRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper.ProfileTargetScoringCharacteristicsRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper.FeedstockProfileEvidenceRestMapper;
import org.craft.mcs.cap.shared.application.query.QueryLimits;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.mapper
    .ProfileTargetCandidateCommonRestMapper.toFeedstockWeights;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper.FeedstockCriteriaRestMapper
    .toPlanningBatchCriteria;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper.FeedstockCriteriaRestMapper
    .toSeason;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.mapper.JolicoeurSugarAcidityGraphCriteriaRestMapper
    .toFeedstockPlanningGraphCriteria;


/** Maps the Feedstock profile-target scoring transport contract. */
public final class FeedstockBatchProfileTargetScoreRestMapper {

  private FeedstockBatchProfileTargetScoreRestMapper() {
  }

  public static @NonNull FeedstockBatchProfileTargetQuery toCommand(
      @NonNull ScoreFeedstockBatchProfileTargetRequest request
  ) {
    requireNonNull(request, "score feedstock batch profile target request cannot be null");
    final var target = requireNonNull(request.target(), "feedstock batch profile target cannot be null");
    final var analysisScenario = requireNonNull(
        request.analysisScenario(),
        "profile target analysis scenario cannot be null"
    );
    final var requestedZone = request.zone();

    return new FeedstockBatchProfileTargetQuery(
        WorkspaceId.of(request.workspaceId()),
        new FeedstockPlanningProfileFilters(
            toPlanningBatchCriteria(request.batchId()),
            toSeason(request.season())
        ),
        toFeedstockPlanningGraphCriteria(
            requestedZone == null
                ? null
                : org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.JolicoeurSugarAcidityZone
                    .valueOf(requestedZone.name()),
            null
        ),
        FeedstockTargetFitConfiguration.AnalysisScenario.valueOf(analysisScenario.name()),
        FeedstockProfileTargetRestMapper.toSelection(target),
        FeedstockProfileTargetRestMapper.toTanninTarget(request.tanninTarget()),
        toFeedstockWeights(request.weights()),
        requireNonNullElse(request.limit(), QueryLimits.DEFAULT)
    );
  }

  public static @NonNull FeedstockBatchProfileTargetScoreBatchResponse toResponse(
      @NonNull FeedstockBatchProfileTargetScores scores
  ) {
    requireNonNull(scores, "feedstock batch profile target scores cannot be null");

    return new FeedstockBatchProfileTargetScoreBatchResponse(
        org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetAnalysisScenario
            .valueOf(scores.analysisScenario().name()),
        org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileAxisNormalizerCode
            .valueOf(scores.normalizerCode().name()),
        scores.scores().stream()
            .map(FeedstockBatchProfileTargetScoreRestMapper::toResponse)
            .toList()
    );
  }

  private static @NonNull FeedstockBatchProfileTargetScoreResponse toResponse(
      @NonNull FeedstockBatchProfileTargetScore score
  ) {
    requireNonNull(score, "feedstock batch profile target score cannot be null");

    return new FeedstockBatchProfileTargetScoreResponse(
        score.batchId().value(),
        score.workspaceId().value(),
        score.currentMaterialStateId().value(),
        score.profileEvidence().stream()
            .map(FeedstockProfileEvidenceRestMapper::toResponse)
            .toList(),
        ProfileTargetScoringCharacteristicsRestMapper.toResponse(score.characteristics()),
        ProfileTargetRestMapper.toResponse(score.distance())
    );
  }
}
