package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.mapper;

import org.craft.mcs.cap.core.feedstock.planning.application.batchoverview.port.in.FeedstockPlanningBatchViewQuery;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetAnalysisScenario;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper.FeedstockProfileTargetRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto.FeedstockWorkspaceBatchQueryRequest;
import org.craft.mcs.cap.shared.application.query.QueryLimits;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper.FeedstockCriteriaRestMapper.toSeason;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.mapper
    .ProfileTargetCandidateCommonRestMapper.toFeedstockWeights;


/** Maps the Feedstock planning query and applies transport defaults. */
public final class FeedstockWorkspaceQueryRestMapper {

  private FeedstockWorkspaceQueryRestMapper() {
  }

  public static @NonNull FeedstockPlanningBatchViewQuery toQuery(
      @NonNull FeedstockWorkspaceBatchQueryRequest request
  ) {
    requireNonNull(request, "feedstock workspace batch view request cannot be null");
    final var requestedAnalysisScenario = request.analysisScenario();
    final var analysisScenario = requestedAnalysisScenario == null
        ? ProfileTargetAnalysisScenario.BLENDING_PLANNING
        : requestedAnalysisScenario;
    final var requestedLimit = request.targetCandidateLimit();
    final var targetCandidateLimit = requestedLimit == null ? QueryLimits.MAXIMUM : requestedLimit;
    return new FeedstockPlanningBatchViewQuery(
        WorkspaceId.of(request.workspaceId()),
        toSeason(request.season()),
        FeedstockProfileTargetRestMapper.toOptionalSelection(request.target()),
        FeedstockProfileTargetRestMapper.toTanninTarget(request.tanninTarget()),
        toFeedstockWeights(request.weights()),
        FeedstockTargetFitConfiguration.AnalysisScenario.valueOf(analysisScenario.name()),
        targetCandidateLimit
    );
  }
}
