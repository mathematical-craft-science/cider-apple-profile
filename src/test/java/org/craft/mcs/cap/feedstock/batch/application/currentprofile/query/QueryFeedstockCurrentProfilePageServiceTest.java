package org.craft.mcs.cap.feedstock.batch.application.currentprofile.query;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import org.craft.mcs.cap.core.feedstock.batch.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.batch.application.api.batch.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.out.FeedstockBatchSummaryQueryRepository;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfilePageItem;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfilePageQuery;
import org.craft.mcs.cap.core.feedstock.batch.application.currentprofile.port.out.FeedstockCurrentProfilePageProvider;
import org.craft.mcs.cap.core.feedstock.batch.application.currentprofile.query.QueryFeedstockCurrentProfilePageService;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetName;
import org.craft.mcs.cap.shared.application.query.QueryLimits;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.shared.testing.FeedstockCurrentProfileViews.currentProfile;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementInstant;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementRecordedAt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class QueryFeedstockCurrentProfilePageServiceTest {

  @Test
  void returnsOnlyProfilesCoherentWithCurrentBatchState() {
    final var workspaceId = WorkspaceId.newId();
    final var coherentBatchId = FeedstockBatchId.newId();
    final var staleBatchId = FeedstockBatchId.newId();
    final var coherentStateId = MaterialStateId.newId();
    final var pageProvider = mock(FeedstockCurrentProfilePageProvider.class);
    final var batchRepository = mock(FeedstockBatchSummaryQueryRepository.class);
    final var accessControl = mock(FeedstockAccessControl.class);
    final var service = new QueryFeedstockCurrentProfilePageService(
        pageProvider,
        batchRepository,
        accessControl
    );
    final var query = query(workspaceId);
    final var coherentBatch = batch(coherentBatchId, workspaceId, coherentStateId, "120");
    final var staleBatch = batch(staleBatchId, workspaceId, MaterialStateId.newId(), "80");
    final var coherentProfile = currentProfile(measurement(coherentBatchId), coherentStateId);
    final var staleProfile = currentProfile(measurement(staleBatchId), MaterialStateId.newId());
    when(batchRepository.findAllByWorkspaceId(workspaceId))
        .thenReturn(List.of(coherentBatch, staleBatch));
    when(pageProvider.findProfiles(query)).thenReturn(List.of(coherentProfile, staleProfile));

    assertThat(service.query(query))
        .singleElement()
        .returns(coherentBatch, FeedstockCurrentProfilePageItem::batch)
        .returns(coherentProfile, FeedstockCurrentProfilePageItem::currentProfile);
    verify(accessControl).requireReadAccess(workspaceId);
  }

  @Test
  void continuesAfterAFullPageContainsOnlyAReplacedMaterialState() {
    final var workspaceId = WorkspaceId.newId();
    final var staleBatchId = FeedstockBatchId.newId();
    final var coherentBatchId = FeedstockBatchId.newId();
    final var coherentStateId = MaterialStateId.newId();
    final var pageProvider = mock(FeedstockCurrentProfilePageProvider.class);
    final var batchRepository = mock(FeedstockBatchSummaryQueryRepository.class);
    final var service = new QueryFeedstockCurrentProfilePageService(
        pageProvider,
        batchRepository,
        mock(FeedstockAccessControl.class)
    );
    final var staleProfile = currentProfile(measurement(staleBatchId), MaterialStateId.newId());
    final var coherentProfile = currentProfile(measurement(coherentBatchId), coherentStateId);
    when(batchRepository.findAllByWorkspaceId(workspaceId)).thenReturn(List.of(
        batch(staleBatchId, workspaceId, MaterialStateId.newId(), "40"),
        batch(coherentBatchId, workspaceId, coherentStateId, "60")
    ));
    when(pageProvider.findProfiles(org.mockito.ArgumentMatchers.any()))
        .thenReturn(List.of(staleProfile))
        .thenReturn(List.of(coherentProfile));
    final var query = new FeedstockCurrentProfilePageQuery(workspaceId, null, null, null, 1);

    assertThat(service.query(query))
        .singleElement()
        .extracting(FeedstockCurrentProfilePageItem::currentProfile)
        .isEqualTo(coherentProfile);
    verify(pageProvider, times(2)).findProfiles(org.mockito.ArgumentMatchers.any());
  }

  private static FeedstockCurrentProfilePageQuery query(WorkspaceId workspaceId) {
    return new FeedstockCurrentProfilePageQuery(
        workspaceId,
        null,
        null,
        null,
        QueryLimits.MAXIMUM_PAGE_FETCH
    );
  }

  private static FeedstockBatchSummary batch(
      FeedstockBatchId batchId,
      WorkspaceId workspaceId,
      MaterialStateId materialStateId,
      String volume
  ) {
    return new FeedstockBatchSummary(
        batchId,
        workspaceId,
        BatchName.of("Current feedstock"),
        VesselTestFixtures.FEEDSTOCK_VESSEL_ID,
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.of(2026, Month.AUGUST, 1), LocalDate.of(2026, Month.DECEMBER, 31)),
        null,
        BatchLifecycle.ACTIVE,
        VolumeValue.ofLiters(volume),
        materialStateId,
        null
    );
  }

  private static FeedstockMeasurementFixture measurement(FeedstockBatchId batchId) {
    return FeedstockMeasurementFixture.create(
        MeasurementSetId.newId(),
        batchId,
        MeasurementSetName.of("Current SG"),
        ProfileCharacteristicsFixture.builder()
            .specificGravity(PointCharacteristicValue.of(SpecificGravityValue.of("1.052")))
            .build(),
        feedstockMeasurementInstant(),
        feedstockMeasurementRecordedAt()
    );
  }
}
