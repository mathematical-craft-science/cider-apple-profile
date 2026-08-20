package org.craft.mcs.cap.feedstock.planning.application.batchoverview.usecase;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.support.CiderAnalysisTestFixtures.feedstockGraphAnalyzer;
import static org.craft.mcs.cap.support.CiderAnalysisTestFixtures.feedstockPhPlanningAnalyzer;
import static org.craft.mcs.cap.support.CiderAnalysisTestFixtures.feedstockPotentialAbvAnalyzer;
import static org.craft.mcs.cap.support.CiderAnalysisTestFixtures.feedstockReadinessAnalyzer;
import static org.craft.mcs.cap.support.CiderAnalysisTestFixtures.feedstockTanninLevelAnalyzer;
import static org.craft.mcs.cap.shared.testing.FeedstockCurrentProfileViews.currentProfile;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementRecordedAt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.craft.mcs.cap.core.feedstock.batch.application.api.batch.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.batch.application.api.batch.FeedstockBatchView;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileDataQualityStatus;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileDataQualityView;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileFreshnessView;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockPlanningReadinessStatus;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockPlanningReadinessView;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.readiness.contract.FeedstockFermentationStartReadinessView;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.sugaracidity.contract.FeedstockSugarAcidityGraphView;
import org.craft.mcs.cap.core.feedstock.planning.application.graph.contract.FeedstockPlanningGraphView.Zone;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.in.ListFeedstockBatchesUseCase;
import org.craft.mcs.cap.core.feedstock.batch.application.currentprofile.port.out.FeedstockCurrentProfileQueryRepository;
import org.craft.mcs.cap.core.feedstock.batch.application.currentprofile.query.FeedstockCurrentProfileQueryService;
import org.craft.mcs.cap.core.feedstock.batch.application.assessment.service.FeedstockCurrentProfileAssessmentService;
import org.craft.mcs.cap.core.feedstock.batch.application.currentprofile.service.FeedstockCurrentProfileFreshnessAssessor;
import org.craft.mcs.cap.core.feedstock.batch.application.assessment.service.FeedstockPlanningReadinessAssessor;
import org.craft.mcs.cap.core.feedstock.batch.application.assessment.service.FeedstockProfileDataQualityAssessor;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.core.feedstock.planning.application.authorization.port.out.FeedstockPlanningAccessControl;
import org.craft.mcs.cap.core.feedstock.planning.application.batchoverview.contract.FeedstockPlanningBatchView;
import org.craft.mcs.cap.core.feedstock.planning.application.batchoverview.contract.FeedstockPlanningTargetAnalysisView;
import org.craft.mcs.cap.core.feedstock.planning.application.batchoverview.port.in.FeedstockPlanningBatchViewQuery;
import org.craft.mcs.cap.core.feedstock.planning.application.batchoverview.usecase.QueryFeedstockPlanningBatchesService;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in.FeedstockBatchProfileTargetCandidate;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in.FeedstockBatchProfileTargetCandidates;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.PredefinedFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in.QueryFeedstockBatchProfileTargetCandidatesUseCase;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockContributionView;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetCharacteristics;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetDistanceView;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration.AnalysisScenario;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration.Normalizer;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration.ObjectiveWeights;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetName;
import org.craft.mcs.cap.infrastructure.adapter.out.feedstock.planning.batch.FeedstockPlanningBatchSnapshotAdapter;
import org.craft.mcs.cap.shared.application.measurement.MeasurementFreshnessStatus;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchLifecycle;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.testing.FeedstockMeasurementFixture;
import org.craft.mcs.cap.shared.testing.ProfileCharacteristicsFixture;
import org.craft.mcs.cap.testsupport.VesselTestFixtures;
import org.junit.jupiter.api.Test;


class QueryFeedstockPlanningBatchesServiceTest {

  private static final WorkspaceId WORKSPACE_ID = WorkspaceId.of(
      UUID.fromString("7c18963c-26d7-45c0-af2c-07998f4f6d6b")
  );
  private static final FeedstockSeason SEASON = FeedstockSeason.of(
      LocalDate.of(2026, Month.SEPTEMBER, 1),
      LocalDate.of(2026, Month.DECEMBER, 31)
  );

  @Test
  void returnsPlanningReadModelForBatchWithCurrentMeasurement() {
    final var batchId = FeedstockBatchId.newId();
    final var batch = batch(batchId);
    final var measurement = measurement(batchId);
    final var listBatches = mock(ListFeedstockBatchesUseCase.class);
    final var currentProfileRepository = mock(FeedstockCurrentProfileQueryRepository.class);
    final var targetCandidates = mock(QueryFeedstockBatchProfileTargetCandidatesUseCase.class);
    final var service = service(listBatches, currentProfileRepository, targetCandidates);
    when(listBatches.list(any())).thenReturn(List.of(batch));
    when(currentProfileRepository.findByBatchIds(Set.of(batchId)))
        .thenReturn(List.of(currentProfile(measurement, batch.batch().currentMaterialStateId())));

    final var view = service.query(queryWithoutTarget());

    assertThat(view.targetAnalysis()).isNull();
    assertThat(view.batches()).hasSize(1);
    final var row = view.batches().getFirst();
    final var assessment = row.currentProfileAssessment();
    assertThat(row.batch()).isEqualTo(batch);
    assertThat(row.currentProfileSummary()).isNotNull();
    assertThat(assessment.freshness())
        .isNotNull()
        .extracting(FeedstockCurrentProfileFreshnessView::status)
        .isEqualTo(MeasurementFreshnessStatus.CURRENT);
    assertThat(assessment.graphEvaluation())
        .isNotNull()
        .returns(true, FeedstockSugarAcidityGraphView::plottable)
        .extracting(FeedstockSugarAcidityGraphView::zoneAssessment)
        .isNotNull();
    assertThat(assessment.potentialAbv()).isNotNull();
    assertThat(requireNonNull(row.currentProfileSummary()).yeastAssimilableNitrogen())
        .isNotNull()
        .returns(
            YeastAssimilableNitrogenValue.of("120"),
            value -> ((PointCharacteristicValue<?>) value).value()
        );
    assertThat(assessment.planningReadiness())
        .isNotNull()
        .extracting(FeedstockPlanningReadinessView::status)
        .isEqualTo(FeedstockPlanningReadinessStatus.NEEDS_ATTENTION);
    assertThat(assessment.dataQuality())
        .isNotNull()
        .extracting(FeedstockCurrentProfileDataQualityView::status)
        .isEqualTo(FeedstockCurrentProfileDataQualityStatus.NEEDS_ATTENTION);
    assertThat(assessment.fermentationStartReadiness())
        .isNotNull()
        .returns(
            FeedstockFermentationStartReadinessView.Status.NEEDS_ATTENTION,
            FeedstockFermentationStartReadinessView::status
        );
    assertThat(row.targetCandidate()).isNull();
    verifyNoInteractions(targetCandidates);
  }

  @Test
  void returnsBaseRowsAndTargetMetadataWhenNoCandidateIsScorable() {
    final var batchId = FeedstockBatchId.newId();
    final var batch = batch(batchId);
    final var listBatches = mock(ListFeedstockBatchesUseCase.class);
    final var currentProfileRepository = mock(FeedstockCurrentProfileQueryRepository.class);
    final var targetCandidates = mock(QueryFeedstockBatchProfileTargetCandidatesUseCase.class);
    final var service = service(listBatches, currentProfileRepository, targetCandidates);
    when(listBatches.list(any())).thenReturn(List.of(batch));
    when(currentProfileRepository.findByBatchIds(Set.of(batchId)))
        .thenReturn(List.of(currentProfile(measurement(batchId), batch.batch().currentMaterialStateId())));
    when(targetCandidates.query(any())).thenReturn(new FeedstockBatchProfileTargetCandidates(
        AnalysisScenario.BLENDING_PLANNING,
        Normalizer.EFFECTIVE_RANGE,
        List.of()
    ));

    final var view = service.query(queryWithTarget());

    assertThat(view)
        .satisfies(result -> assertThat(result.batches())
            .singleElement()
            .returns(null, FeedstockPlanningBatchView::targetCandidate))
        .satisfies(result -> assertThat(result.targetAnalysis())
            .isNotNull()
            .returns(
                AnalysisScenario.BLENDING_PLANNING,
                FeedstockPlanningTargetAnalysisView::analysisScenario
            )
            .returns(
                Normalizer.EFFECTIVE_RANGE,
                FeedstockPlanningTargetAnalysisView::normalizerCode
            ));
  }

  @Test
  void omitsTargetCandidateCalculatedForAReplacedMaterialState() {
    final var batchId = FeedstockBatchId.newId();
    final var batch = batch(batchId);
    final var currentProfile = currentProfile(measurement(batchId), batch.batch().currentMaterialStateId());
    final var listBatches = mock(ListFeedstockBatchesUseCase.class);
    final var currentProfileRepository = mock(FeedstockCurrentProfileQueryRepository.class);
    final var targetCandidates = mock(QueryFeedstockBatchProfileTargetCandidatesUseCase.class);
    final var service = service(listBatches, currentProfileRepository, targetCandidates);
    when(listBatches.list(any())).thenReturn(List.of(batch));
    when(currentProfileRepository.findByBatchIds(Set.of(batchId))).thenReturn(List.of(currentProfile));
    when(targetCandidates.query(any())).thenReturn(new FeedstockBatchProfileTargetCandidates(
        AnalysisScenario.BLENDING_PLANNING,
        Normalizer.EFFECTIVE_RANGE,
        List.of(new FeedstockBatchProfileTargetCandidate(
            batchId,
            MaterialStateId.newId(),
            currentProfile.evidence(),
            mock(FeedstockTargetCharacteristics.class),
            mock(FeedstockTargetDistanceView.class),
            mock(FeedstockContributionView.class)
        ))
    ));

    final var view = service.query(queryWithTarget());

    assertThat(view.batches())
        .singleElement()
        .extracting(FeedstockPlanningBatchView::targetCandidate)
        .isNull();
  }

  @Test
  void reportsFreshnessAlreadyAssessedByMeasurementProjection() {
    final var batchId = FeedstockBatchId.newId();
    final var batch = batch(batchId);
    final var measurement = measurement(batchId);
    final var listBatches = mock(ListFeedstockBatchesUseCase.class);
    final var currentProfileRepository = mock(FeedstockCurrentProfileQueryRepository.class);
    final var targetCandidates = mock(QueryFeedstockBatchProfileTargetCandidatesUseCase.class);
    final var service = service(listBatches, currentProfileRepository, targetCandidates);
    when(listBatches.list(any())).thenReturn(List.of(batch));
    when(currentProfileRepository.findByBatchIds(Set.of(batchId)))
        .thenReturn(List.of(currentProfile(
            measurement,
            batch.batch().currentMaterialStateId(),
            MeasurementFreshnessStatus.STALE
        )));

    final var view = service.query(queryWithoutTarget());

    assertThat(view.batches())
        .singleElement()
        .satisfies(row -> assertThat(row.currentProfileAssessment().freshness())
            .isNotNull()
            .satisfies(freshness -> assertThat(freshness)
                .returns(
                    MeasurementFreshnessStatus.STALE,
                    FeedstockCurrentProfileFreshnessView::status
                )
                .returns(true, FeedstockCurrentProfileFreshnessView::stale)
                .returns(
                    "Current profile contains planning evidence older than the configured freshness threshold",
                    FeedstockCurrentProfileFreshnessView::reason
                )));
  }

  @Test
  void omitsCurrentProfileCalculatedForAReplacedMaterialState() {
    final var batchId = FeedstockBatchId.newId();
    final var batch = batch(batchId);
    final var listBatches = mock(ListFeedstockBatchesUseCase.class);
    final var currentProfileRepository = mock(FeedstockCurrentProfileQueryRepository.class);
    final var targetCandidates = mock(QueryFeedstockBatchProfileTargetCandidatesUseCase.class);
    final var service = service(listBatches, currentProfileRepository, targetCandidates);
    when(listBatches.list(any())).thenReturn(List.of(batch));
    when(currentProfileRepository.findByBatchIds(Set.of(batchId)))
        .thenReturn(List.of(currentProfile(measurement(batchId), MaterialStateId.newId())));

    final var view = service.query(queryWithoutTarget());

    assertThat(view.batches())
        .singleElement()
        .returns(null, FeedstockPlanningBatchView::currentProfileSummary)
        .satisfies(row -> assertThat(row.currentProfileAssessment().planningReadiness())
            .returns(
                FeedstockPlanningReadinessStatus.BLOCKED,
                FeedstockPlanningReadinessView::status
            ));
  }

  private static QueryFeedstockPlanningBatchesService service(
      ListFeedstockBatchesUseCase listBatches,
      FeedstockCurrentProfileQueryRepository currentProfileRepository,
      QueryFeedstockBatchProfileTargetCandidatesUseCase targetCandidates
  ) {
    final var profileQuery = new FeedstockCurrentProfileQueryService(currentProfileRepository);
    final var assessmentService = new FeedstockCurrentProfileAssessmentService(
        feedstockGraphAnalyzer(),
        feedstockTanninLevelAnalyzer(),
        feedstockPotentialAbvAnalyzer(),
        feedstockReadinessAnalyzer(),
        new FeedstockCurrentProfileFreshnessAssessor(),
        new FeedstockPlanningReadinessAssessor(feedstockPhPlanningAnalyzer()),
        new FeedstockProfileDataQualityAssessor()
    );
    final var snapshotProvider = new FeedstockPlanningBatchSnapshotAdapter(
        listBatches,
        profileQuery,
        assessmentService
    );
    return new QueryFeedstockPlanningBatchesService(
        snapshotProvider,
        targetCandidates,
        mock(FeedstockPlanningAccessControl.class)
    );
  }

  private static FeedstockPlanningBatchViewQuery queryWithoutTarget() {
    return new FeedstockPlanningBatchViewQuery(
        WORKSPACE_ID,
        SEASON,
        null,
        null,
        ObjectiveWeights.balanced(),
        AnalysisScenario.BLENDING_PLANNING,
        200
    );
  }

  private static FeedstockPlanningBatchViewQuery queryWithTarget() {
    return new FeedstockPlanningBatchViewQuery(
        WORKSPACE_ID,
        SEASON,
        new PredefinedFeedstockProfileTargetSelection(
            Zone.PERFECT_APPLES
        ),
        null,
        ObjectiveWeights.balanced(),
        AnalysisScenario.BLENDING_PLANNING,
        200
    );
  }

  private static FeedstockBatchView batch(FeedstockBatchId batchId) {
    final var summary = new FeedstockBatchSummary(
        batchId,
        WORKSPACE_ID,
        BatchName.of("Late season juice"),
        VesselTestFixtures.vesselId("Tank A-01"),
        FeedstockType.JUICE,
        SEASON,
        null,
        BatchLifecycle.ACTIVE,
        VolumeValue.ofLiters("120"),
        MaterialStateId.newId(),
        null
    );
    return new FeedstockBatchView(
        summary,
        VesselTestFixtures.reference(VesselTestFixtures.FEEDSTOCK_VESSEL_ID, "Tank A-01")
    );
  }

  private static FeedstockMeasurementFixture measurement(FeedstockBatchId batchId) {
    return FeedstockMeasurementFixture.create(
        MeasurementSetId.newId(),
        batchId,
        MeasurementSetName.of("Current profile"),
        ProfileCharacteristicsFixture.builder()
            .specificGravity(PointCharacteristicValue.of(SpecificGravityValue.of("1.060")))
            .titratableAcidity(PointCharacteristicValue.of(TitratableAcidityValue.of("5.0")))
            .yeastAssimilableNitrogen(PointCharacteristicValue.of(YeastAssimilableNitrogenValue.of("120")))
            .build(),
        Instant.parse("2026-05-01T10:00:00Z"),
        feedstockMeasurementRecordedAt()
    );
  }
}
