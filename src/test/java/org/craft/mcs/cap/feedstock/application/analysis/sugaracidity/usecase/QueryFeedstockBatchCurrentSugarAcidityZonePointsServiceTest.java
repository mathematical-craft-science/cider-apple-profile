package org.craft.mcs.cap.feedstock.application.analysis.sugaracidity.usecase;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileProjector;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.BuiltInSugarAcidityZoneProfiles;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.RectangleSugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.craft.mcs.cap.core.feedstock.application.measurement.query.FeedstockBatchCurrentMeasurementQueryService;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockBatchCurrentSugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockBatchCurrentSugarAcidityZoneResult;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockMeasurementPointFilters;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.out.LatestFeedstockMeasurementSugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.usecase.QueryFeedstockBatchCurrentSugarAcidityZonePointsService;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchSummaryQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.out.FeedstockMeasurementQueryRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.shared.kernel.material.value.Vessel;
import org.craft.mcs.cap.shared.testing.FeedstockMeasurementFixture;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementName;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.craft.mcs.cap.shared.application.query.pagination.CursorPageRequest;
import org.craft.mcs.cap.shared.application.query.QueryLimits;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.craft.mcs.cap.shared.testing.FeedstockMeasurementViews.view;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementInstant;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementRecordedAt;
import static org.craft.mcs.cap.core.feedstock.application.batch.usecase.FeedstockBatchSummaryMapper.toSummary;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class QueryFeedstockBatchCurrentSugarAcidityZonePointsServiceTest {

  @Test
  void queriesLatestMeasurementPerBatchWithLookaheadAtMaximumPageLimit() {
    final var batchRepository = mock(FeedstockBatchSummaryQueryRepository.class);
    final var repository = mock(FeedstockMeasurementQueryRepository.class);
    final var service = service(batchRepository, repository);
    final var workspaceId = WorkspaceId.of(newUuid());
    final var batch = batch(workspaceId);
    final var current = measurement("Current", batch.batchId());

    when(batchRepository.findAllByWorkspaceId(workspaceId)).thenReturn(List.of(toSummary(batch)));
    when(repository.findLatestSugarAcidityPointCandidates(any())).thenReturn(List.of(view(current)));

    final var result = service.query(new FeedstockBatchCurrentSugarAcidityZonePointQuery(
        workspaceId,
        new FeedstockMeasurementPointFilters(null, null, null, null),
        CursorPageRequest.firstPage(QueryLimits.MAXIMUM)
    ));

    final var candidateQuery = ArgumentCaptor.forClass(
        LatestFeedstockMeasurementSugarAcidityPointCandidateQuery.class
    );
    verify(repository).findLatestSugarAcidityPointCandidates(candidateQuery.capture());

    assertThat(candidateQuery.getValue())
        .returns(QueryLimits.MAXIMUM_PAGE_FETCH, LatestFeedstockMeasurementSugarAcidityPointCandidateQuery::limit);
    assertThat(result.items())
        .singleElement()
        .returns(current.measurementId(), FeedstockBatchCurrentSugarAcidityZoneResult::currentMeasurementId)
        .returns(batch.name(), FeedstockBatchCurrentSugarAcidityZoneResult::batchName)
        .returns(current.measuredAt(), FeedstockBatchCurrentSugarAcidityZoneResult::measuredAt)
        .returns(
            SugarAcidityZone.PERFECT_APPLES,
            item -> item.evaluation().assessment().zone()
        );
  }

  private static QueryFeedstockBatchCurrentSugarAcidityZonePointsService service(
      FeedstockBatchSummaryQueryRepository batchRepository,
      FeedstockMeasurementQueryRepository repository
  ) {
    return new QueryFeedstockBatchCurrentSugarAcidityZonePointsService(
        batchRepository,
        mock(FeedstockAccessControl.class),
        new FeedstockBatchCurrentMeasurementQueryService(repository),
        projector()
    );
  }

  private static SugarAcidityProfileProjector projector() {
    return new SugarAcidityProfileProjector(
        new ChemicalProfileAxisExtractor(),
        new ChemicalProfileCapabilityAssessor(),
        new RectangleSugarAcidityZoneAssessor(
            BuiltInSugarAcidityZoneProfiles.thresholds(),
            BuiltInSugarAcidityZoneProfiles.rectangularZoneGeometryProfile()
        )
    );
  }

  private static FeedstockMeasurementFixture measurement(
      String name,
      FeedstockBatchId batchId
  ) {
    return FeedstockMeasurementFixture.create(
        FeedstockMeasurementId.newId(),
        batchId,
        FeedstockMeasurementName.of(name),
        ChemicalProfile.builder()
            .sugar(PointCharacteristicValue.of(SugarConcentration.ofSpecificGravity(
                SpecificGravityValue.of("1.066")
            )))
            .titratableAcidity(PointCharacteristicValue.of(TitratableAcidityValue.of("5.7")))
            .build(),
        feedstockMeasurementInstant(),
        feedstockMeasurementRecordedAt()
    );
  }

  private static FeedstockBatch batch(WorkspaceId workspaceId) {
    return FeedstockBatch.create(
        FeedstockBatchId.newId(),
        workspaceId,
        BatchName.of("Late season juice"),
        Vessel.named("Tank A-01"),
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.of(2026, Month.SEPTEMBER, 1), LocalDate.of(2026, Month.DECEMBER, 31)),
        VolumeValue.ofLiters("100")
    );
  }
}
