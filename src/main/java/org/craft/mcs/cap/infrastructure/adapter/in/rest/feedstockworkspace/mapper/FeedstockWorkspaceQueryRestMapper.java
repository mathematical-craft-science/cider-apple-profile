package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.mapper;

import org.craft.mcs.cap.core.feedstock.planning.application.batchoverview.port.in.FeedstockPlanningBatchViewQuery;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.model.ProfileTargetAnalysisScenario;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper.FeedstockProfileTargetRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto.FeedstockWorkspaceBatchQueryRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper.ProfileTargetRestMapper;
import org.craft.mcs.cap.shared.application.query.QueryLimits;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper.FeedstockCriteriaRestMapper.toSeason;


/** Maps the Feedstock planning query and applies transport defaults. */
public final class FeedstockWorkspaceQueryRestMapper {

  private FeedstockWorkspaceQueryRestMapper() {
  }

  public static @NonNull FeedstockPlanningBatchViewQuery toQuery(
      @NonNull FeedstockWorkspaceBatchQueryRequest request
  ) {
    requireNonNull(request, "feedstock workspace batch view request cannot be null");
    final var analysisScenario = requireNonNullElse(
        request.analysisScenario(),
        ProfileTargetAnalysisScenario.BLENDING_PLANNING
    );
    return new FeedstockPlanningBatchViewQuery(
        WorkspaceId.of(request.workspaceId()),
        toSeason(request.season()),
        FeedstockProfileTargetRestMapper.toOptionalSelection(request.target()),
        FeedstockProfileTargetRestMapper.toTanninTarget(request.tanninTarget()),
        ProfileTargetRestMapper.toWeights(request.weights()),
        requireNonNull(analysisScenario, "profile target analysis scenario cannot be null"),
        requireNonNullElse(request.targetCandidateLimit(), QueryLimits.MAXIMUM)
    );
  }
}
