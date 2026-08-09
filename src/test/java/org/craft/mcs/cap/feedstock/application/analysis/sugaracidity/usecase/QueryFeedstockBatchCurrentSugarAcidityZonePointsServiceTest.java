package org.craft.mcs.cap.feedstock.application.analysis.sugaracidity.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.core.feedstock.application.batch.usecase.FeedstockBatchSummaryMapper.toSummary;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures
    .graphEvaluator;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures
    .northAmericanProfileCalculator;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.craft.mcs.cap.shared.testing.FeedstockCurrentProfileViews.currentProfile;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementInstant;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementRecordedAt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.model.FeedstockAnalysisCandidateFilters;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.port.out.CurrentFeedstockBatchAnalysisCandidateQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.port.out.FeedstockCurrentProfileAnalysisCandidateRepository;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockBatchCurrentSugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockBatchCurrentSugarAcidityZoneResult;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.usecase.QueryFeedstockBatchCurrentSugarAcidityZonePointsService;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchSummaryQueryRepository;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZone;
import org.craft.mcs.cap.shared.application.query.QueryLimits;
import org.craft.mcs.cap.shared.application.query.pagination.CursorPageRequest;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.testing.ProfileCharacteristicsFixture;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.shared.kernel.material.value.Vessel;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.testing.FeedstockMeasurementFixture;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;


class QueryFeedstockBatchCurrentSugarAcidityZonePointsServiceTest {

  @Test
  void queriesLatestMeasurementPerBatchWithLookaheadAtMaximumPageLimit() {
    final var batchRepository = mock(FeedstockBatchSummaryQueryRepository.class);
    final var repository = mock(FeedstockCurrentProfileAnalysisCandidateRepository.class);
    final var service = service(batchRepository, repository);
    final var workspaceId = WorkspaceId.of(newUuid());
    final var batch = batch(workspaceId);
    final var current = measurement("Current", batch.batchId());

    when(batchRepository.findAllByWorkspaceId(workspaceId)).thenReturn(List.of(toSummary(batch)));
    when(repository.findCandidates(any())).thenReturn(List.of(currentProfile(current)));

    final var result = service.query(new FeedstockBatchCurrentSugarAcidityZonePointQuery(
        workspaceId,
        new FeedstockAnalysisCandidateFilters(null, null),
        null,
        CursorPageRequest.firstPage(QueryLimits.MAXIMUM)
    ));

    final var candidateQuery = ArgumentCaptor.forClass(
        CurrentFeedstockBatchAnalysisCandidateQuery.class
    );
    verify(repository).findCandidates(candidateQuery.capture());

    assertThat(candidateQuery.getValue())
        .returns(QueryLimits.MAXIMUM_PAGE_FETCH, CurrentFeedstockBatchAnalysisCandidateQuery::limit);
    assertThat(result.items())
        .singleElement()
        .returns(batch.name(), FeedstockBatchCurrentSugarAcidityZoneResult::batchName)
        .satisfies(item -> assertThat(item.profileEvidence())
            .extracting(evidence -> evidence.evidenceReference().measurementSetId())
            .containsOnly(current.measurementId().value()))
        .returns(
            JolicoeurSugarAcidityZone.PERFECT_APPLES,
            item -> item.graphEvaluation().zoneAssessment().zone()
        );
  }

  private static QueryFeedstockBatchCurrentSugarAcidityZonePointsService service(
      FeedstockBatchSummaryQueryRepository batchRepository,
      FeedstockCurrentProfileAnalysisCandidateRepository repository
  ) {
    return new QueryFeedstockBatchCurrentSugarAcidityZonePointsService(
        batchRepository,
        mock(FeedstockAccessControl.class),
        repository,
        graphEvaluator(),
        northAmericanProfileCalculator()
    );
  }

  private static FeedstockMeasurementFixture measurement(
      String name,
      FeedstockBatchId batchId
  ) {
    return FeedstockMeasurementFixture.create(
        MeasurementSetId.newId(),
        batchId,
        MeasurementSetName.of(name),
        ProfileCharacteristicsFixture.builder()
            .specificGravity(PointCharacteristicValue.of(SpecificGravityValue.of("1.066")))
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
