package org.craft.mcs.cap.infrastructure.adapter.out.feedstock.batch.measurement;

import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfilePageQuery;
import org.craft.mcs.cap.core.feedstock.batch.application.currentprofile.port.out.FeedstockCurrentProfilePageSubjectQuery;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileSubject;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockLatestMeasurementSetView;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class MeasurementFeedstockCurrentProfilePageProviderTest {

  @Test
  void ordersProjectedCandidatesByLatestEvidenceBeforeApplyingTheLimit() {
    final var workspaceId = WorkspaceId.newId();
    final var subjectQuery = mock(FeedstockCurrentProfilePageSubjectQuery.class);
    final var projector = mock(MeasurementFeedstockCurrentProfileProjector.class);
    final var adapter = new MeasurementFeedstockCurrentProfilePageProvider(subjectQuery, projector);
    final var subjects = List.of(subject(workspaceId), subject(workspaceId));
    final var older = profile(Instant.parse("2026-08-01T10:00:00Z"));
    final var newer = profile(Instant.parse("2026-08-02T10:00:00Z"));
    final var query = new FeedstockCurrentProfilePageQuery(
        workspaceId,
        null,
        null,
        null,
        1
    );
    when(subjectQuery.findSubjects(query)).thenReturn(subjects);
    when(projector.project(subjects)).thenReturn(List.of(older, newer));

    assertThat(adapter.findProfiles(query)).containsExactly(newer);
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
