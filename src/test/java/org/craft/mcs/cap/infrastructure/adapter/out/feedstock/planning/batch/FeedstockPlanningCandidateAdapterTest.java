package org.craft.mcs.cap.infrastructure.adapter.out.feedstock.planning.batch;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.craft.mcs.cap.core.feedstock.batch.application.api.batch.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.batch.application.api.batch.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfilePageItem;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfilePageQuery;
import org.craft.mcs.cap.core.feedstock.batch.application.currentprofile.port.in.QueryFeedstockCurrentProfilePageUseCase;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.core.feedstock.planning.application.candidate.model.FeedstockPlanningCandidateQuery;
import org.craft.mcs.cap.core.feedstock.planning.application.candidate.model.FeedstockPlanningCandidateSnapshot;
import org.craft.mcs.cap.core.feedstock.planning.application.candidate.model.FeedstockPlanningBatchCriteria;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetName;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchLifecycle;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.testing.FeedstockMeasurementFixture;
import org.craft.mcs.cap.shared.testing.ProfileCharacteristicsFixture;
import org.craft.mcs.cap.testsupport.VesselTestFixtures;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.shared.testing.FeedstockCurrentProfileViews.currentProfile;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementInstant;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementRecordedAt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FeedstockPlanningCandidateAdapterTest {

  @Test
  void mapsPublishedBatchSnapshotWithoutRecalculatingAvailableVolume() {
    final var workspaceId = WorkspaceId.newId();
    final var batchId = FeedstockBatchId.newId();
    final var stateId = MaterialStateId.newId();
    final var currentProfile = currentProfile(measurement(batchId), stateId);
    final var currentProfilePage = mock(QueryFeedstockCurrentProfilePageUseCase.class);
    final var adapter = new FeedstockPlanningCandidateAdapter(currentProfilePage);
    final var planningQuery = new FeedstockPlanningCandidateQuery(
        workspaceId,
        FeedstockPlanningBatchCriteria.of(batchId),
        null,
        null,
        20
    );
    when(currentProfilePage.query(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(
        new FeedstockCurrentProfilePageItem(
            batch(batchId, workspaceId, stateId),
            currentProfile
        )
    ));

    assertThat(adapter.findCandidates(planningQuery))
        .singleElement()
        .returns(currentProfile, FeedstockPlanningCandidateSnapshot::currentProfile)
        .returns(VolumeValue.ofLiters("75"), FeedstockPlanningCandidateSnapshot::availableVolume);
    final var queryCaptor = ArgumentCaptor.forClass(FeedstockCurrentProfilePageQuery.class);
    verify(currentProfilePage).query(queryCaptor.capture());
    assertThat(queryCaptor.getValue())
        .returns(workspaceId, FeedstockCurrentProfilePageQuery::workspaceId)
        .satisfies(query -> assertThat(query.batch())
            .isNotNull()
            .extracting(FeedstockBatchCriteria::batchIds)
            .isEqualTo(Set.of(batchId)))
        .returns(20, FeedstockCurrentProfilePageQuery::limit);
  }

  private static FeedstockBatchSummary batch(
      FeedstockBatchId batchId,
      WorkspaceId workspaceId,
      MaterialStateId stateId
  ) {
    return new FeedstockBatchSummary(
        batchId,
        workspaceId,
        BatchName.of("Planning source"),
        VesselTestFixtures.FEEDSTOCK_VESSEL_ID,
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 12, 31)),
        null,
        BatchLifecycle.ACTIVE,
        VolumeValue.ofLiters("75"),
        stateId,
        null
    );
  }

  private static FeedstockMeasurementFixture measurement(FeedstockBatchId batchId) {
    return FeedstockMeasurementFixture.create(
        MeasurementSetId.newId(),
        batchId,
        MeasurementSetName.of("Planning SG"),
        ProfileCharacteristicsFixture.builder()
            .specificGravity(PointCharacteristicValue.of(SpecificGravityValue.of("1.054")))
            .build(),
        feedstockMeasurementInstant(),
        feedstockMeasurementRecordedAt()
    );
  }
}
