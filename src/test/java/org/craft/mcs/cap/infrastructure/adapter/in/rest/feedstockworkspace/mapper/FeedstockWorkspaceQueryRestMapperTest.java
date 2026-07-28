package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.mapper;

import java.util.UUID;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningBatchViewQuery;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.model.ProfileTargetAnalysisScenario;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto.FeedstockWorkspaceBatchQueryRequest;
import org.craft.mcs.cap.shared.application.query.QueryLimits;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class FeedstockWorkspaceQueryRestMapperTest {

  @Test
  void appliesPlanningDefaultsWithoutInventingOptionalFilters() {
    final var workspaceId = UUID.fromString("019c5d4e-1f2a-7000-8000-000000000001");
    final var query = FeedstockWorkspaceQueryRestMapper.toQuery(
        new FeedstockWorkspaceBatchQueryRequest(
            workspaceId,
            null,
            null,
            null,
            null,
            null,
            null
        )
    );

    assertThat(query)
        .returns(workspaceId, candidate -> candidate.workspaceId().value())
        .returns(ProfileTargetAnalysisScenario.BLENDING_PLANNING, FeedstockPlanningBatchViewQuery::analysisScenario)
        .returns(QueryLimits.MAXIMUM, FeedstockPlanningBatchViewQuery::targetCandidateLimit)
        .matches(candidate -> candidate.season() == null)
        .matches(candidate -> candidate.target() == null)
        .matches(candidate -> candidate.tanninTarget() == null);
  }
}
