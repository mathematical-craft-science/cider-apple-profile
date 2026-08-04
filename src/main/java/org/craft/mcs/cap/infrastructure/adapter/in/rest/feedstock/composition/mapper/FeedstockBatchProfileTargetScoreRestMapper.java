package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.composition.mapper;

import org.craft.mcs.cap.core.feedstock.application.analysis.common.model.FeedstockAnalysisCandidateFilters;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockBatchProfileTargetQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockBatchProfileTargetScore;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockBatchProfileTargetScores;
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
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper.FeedstockCriteriaRestMapper
    .toBatchCriteria;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper.FeedstockCriteriaRestMapper
    .toSeason;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.mapper.ProfileGraphCriteriaRestMapper
    .toCapabilityCriteria;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.mapper.ProfileGraphCriteriaRestMapper
    .toZoneCriteria;


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

    return new FeedstockBatchProfileTargetQuery(
        WorkspaceId.of(request.workspaceId()),
        new FeedstockAnalysisCandidateFilters(
            toBatchCriteria(request.batchId()),
            toSeason(request.season()),
            toZoneCriteria(request.zone()),
            toCapabilityCriteria(request.capability(), request.capable())
        ),
        analysisScenario,
        FeedstockProfileTargetRestMapper.toSelection(target),
        FeedstockProfileTargetRestMapper.toTanninTarget(request.tanninTarget()),
        ProfileTargetRestMapper.toWeights(request.weights()),
        requireNonNullElse(request.limit(), QueryLimits.DEFAULT)
    );
  }

  public static @NonNull FeedstockBatchProfileTargetScoreBatchResponse toResponse(
      @NonNull FeedstockBatchProfileTargetScores scores
  ) {
    requireNonNull(scores, "feedstock batch profile target scores cannot be null");

    return new FeedstockBatchProfileTargetScoreBatchResponse(
        scores.analysisScenario(),
        scores.normalizerCode(),
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
