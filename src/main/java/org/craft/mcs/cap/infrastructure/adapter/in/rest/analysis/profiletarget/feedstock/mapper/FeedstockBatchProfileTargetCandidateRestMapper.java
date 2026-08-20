package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.feedstock.mapper;

import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in.FeedstockBatchProfileTargetCandidate;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in.FeedstockBatchProfileTargetQuery;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in.FeedstockBatchProfileTargetCandidates;
import org.craft.mcs.cap.core.feedstock.planning.application.candidate.contract.FeedstockPlanningProfileFilters;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.mapper.CompositionContributionRoleRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.feedstock.dto.FeedstockBatchProfileTargetCandidateResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.feedstock.dto.FeedstockBatchProfileTargetCandidateSearchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.feedstock.dto.FeedstockBatchProfileTargetCandidatesResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper.FeedstockProfileTargetRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper.ProfileTargetRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper.ProfileTargetScoringCharacteristicsRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper.FeedstockProfileEvidenceRestMapper;
import org.craft.mcs.cap.shared.application.query.QueryLimits;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.mapper.ProfileTargetCandidateCommonRestMapper.toFeedstockWeights;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper.FeedstockCriteriaRestMapper.toPlanningBatchCriteria;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper.FeedstockCriteriaRestMapper.toSeason;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.mapper.JolicoeurSugarAcidityGraphCriteriaRestMapper
    .toFeedstockPlanningGraphCriteria;


public final class FeedstockBatchProfileTargetCandidateRestMapper {

  private FeedstockBatchProfileTargetCandidateRestMapper() {
  }

  public static @NonNull FeedstockBatchProfileTargetQuery toQuery(
      @NonNull FeedstockBatchProfileTargetCandidateSearchRequest request
  ) {
    requireNonNull(request, "feedstock batch profile target candidate query request cannot be null");
    final var requestedZone = request.zone();

    return new FeedstockBatchProfileTargetQuery(
        WorkspaceId.of(request.workspaceId()),
        new FeedstockPlanningProfileFilters(
            toPlanningBatchCriteria(request.batchIds()),
            toSeason(request.season())
        ),
        toFeedstockPlanningGraphCriteria(
            requestedZone == null
                ? null
                : org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.JolicoeurSugarAcidityZone
                    .valueOf(requestedZone.name()),
            null
        ),
        FeedstockTargetFitConfiguration.AnalysisScenario.valueOf(
            requireNonNull(request.analysisScenario(), "profile target analysis scenario cannot be null").name()
        ),
        FeedstockProfileTargetRestMapper.toSelection(request.target()),
        FeedstockProfileTargetRestMapper.toTanninTarget(request.tanninTarget()),
        toFeedstockWeights(request.weights()),
        requireNonNullElse(request.limit(), QueryLimits.DEFAULT)
    );
  }

  public static @NonNull FeedstockBatchProfileTargetCandidatesResponse toResponse(
      @NonNull FeedstockBatchProfileTargetCandidates candidates
  ) {
    requireNonNull(candidates, "feedstock batch profile target candidates cannot be null");

    return new FeedstockBatchProfileTargetCandidatesResponse(
        org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetAnalysisScenario
            .valueOf(candidates.analysisScenario().name()),
        org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileAxisNormalizerCode
            .valueOf(candidates.normalizerCode().name()),
        candidates.candidates().stream()
            .map(FeedstockBatchProfileTargetCandidateRestMapper::toResponse)
            .toList()
    );
  }

  private static @NonNull FeedstockBatchProfileTargetCandidateResponse toResponse(
      @NonNull FeedstockBatchProfileTargetCandidate candidate
  ) {
    requireNonNull(candidate, "feedstock batch profile target candidate cannot be null");

    return new FeedstockBatchProfileTargetCandidateResponse(
        candidate.batchId().value(),
        candidate.currentMaterialStateId().value(),
        candidate.profileEvidence().stream()
            .map(FeedstockProfileEvidenceRestMapper::toResponse)
            .toList(),
        ProfileTargetScoringCharacteristicsRestMapper.toResponse(candidate.characteristics()),
        ProfileTargetRestMapper.toResponse(candidate.distance()),
        requireNonNull(
            CompositionContributionRoleRestMapper.toResponse(candidate.contributionRole()),
            "composition contribution role response cannot be null"
        )
    );
  }

}
