package org.craft.mcs.cap.infrastructure.adapter.out.feedstock.measurement;

import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.port.out.CurrentFeedstockBatchAnalysisCandidateQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.port.out.FeedstockAnalysisCandidateSubjectQuery;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileSubject;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockLatestMeasurementSetView;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class MeasurementFeedstockAnalysisCandidateQueryTest {

  @Test
  void ordersProjectedCandidatesByLatestEvidenceBeforeApplyingTheLimit() {
    final var workspaceId = WorkspaceId.newId();
    final var subjectQuery = mock(FeedstockAnalysisCandidateSubjectQuery.class);
    final var projector = mock(MeasurementFeedstockCurrentProfileProjector.class);
    final var adapter = new MeasurementFeedstockAnalysisCandidateQuery(subjectQuery, projector);
    final var subjects = List.of(subject(workspaceId), subject(workspaceId));
    final var older = profile(Instant.parse("2026-08-01T10:00:00Z"));
    final var newer = profile(Instant.parse("2026-08-02T10:00:00Z"));
    final var query = new CurrentFeedstockBatchAnalysisCandidateQuery(
        workspaceId,
        null,
        null,
        null,
        1
    );
    when(subjectQuery.findCandidates(query)).thenReturn(subjects);
    when(projector.project(subjects)).thenReturn(List.of(older, newer));

    assertThat(adapter.findCandidates(query)).containsExactly(newer);
  }

  private static FeedstockCurrentProfileSubject subject(WorkspaceId workspaceId) {
    return new FeedstockCurrentProfileSubject(FeedstockBatchId.newId(), workspaceId);
  }

  private static FeedstockCurrentProfileView profile(Instant measuredAt) {
    final var profile = mock(FeedstockCurrentProfileView.class);
    final var latestMeasurementSet = mock(FeedstockLatestMeasurementSetView.class);
    when(profile.latestMeasurementSet()).thenReturn(latestMeasurementSet);
    when(latestMeasurementSet.measuredAt()).thenReturn(measuredAt);
    when(latestMeasurementSet.measurementSetId()).thenReturn(newUuid());
    return profile;
  }
}
