package org.craft.mcs.cap.feedstock.application.planning.batchoverview.usecase;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileFreshnessView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileDataQualityView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockPlanningReadinessView;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.FixedFinalGravityPredictionStrategy;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.StandardGravityDropAbvPredictionStrategy;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessAssessor;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessAssessment;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessStatus;
import org.craft.mcs.cap.core.profileanalysis.graph.application.model.JolicoeurSugarAcidityGraphEvaluation;
import org.craft.mcs.cap.core.profileanalysis.ph.domain.PhPlanningGuidancePolicy;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.model.ProfileTargetAnalysisScenario;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringInput;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment.ProfileTargetDistance;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain.CompositionContributionRoleAssessment;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZone;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockBatchProfileTargetCandidate;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockBatchProfileTargetCandidates;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.ListFeedstockBatchesUseCase;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.PredefinedFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.QueryFeedstockBatchProfileTargetCandidatesUseCase;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.model.FeedstockPlanningBatchView;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningBatchViewQuery;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileDataQualityStatus;
import org.craft.mcs.cap.shared.application.measurement.MeasurementFreshnessStatus;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockPlanningReadinessStatus;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.query.FeedstockCurrentProfileQueryService;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.port.out.FeedstockCurrentProfileQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.service.FeedstockCurrentProfileFreshnessAssessor;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.service.FeedstockCurrentProfileAssessmentService;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.service.FeedstockPlanningReadinessAssessor;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.service.FeedstockProfileDataQualityAssessor;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.usecase.QueryFeedstockPlanningBatchesService;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.shared.kernel.material.value.BatchLifecycle;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.shared.kernel.material.value.Vessel;
import org.craft.mcs.cap.shared.testing.FeedstockMeasurementFixture;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetName;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.testing.ProfileCharacteristicsFixture;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.junit.jupiter.api.Test;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures.graphEvaluator;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures
    .northAmericanProfileCalculator;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementRecordedAt;
import static org.craft.mcs.cap.shared.testing.FeedstockCurrentProfileViews.currentProfile;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


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
        .thenReturn(List.of(currentProfile(measurement, batch.currentMaterialStateId())));

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
        .returns(true, JolicoeurSugarAcidityGraphEvaluation::plottable)
        .extracting(JolicoeurSugarAcidityGraphEvaluation::zoneAssessment)
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
            FermentationStartReadinessStatus.NEEDS_ATTENTION,
            FermentationStartReadinessAssessment::status
        );
    assertThat(row.targetCandidate()).isNull();
    verifyNoInteractions(targetCandidates);
  }

  @Test
  void returnsTargetAnalysisMetadataWhenTargetIsRequested() {
    final var batchId = FeedstockBatchId.newId();
    final var batch = batch(batchId);
    final var listBatches = mock(ListFeedstockBatchesUseCase.class);
    final var currentProfileRepository = mock(FeedstockCurrentProfileQueryRepository.class);
    final var targetCandidates = mock(QueryFeedstockBatchProfileTargetCandidatesUseCase.class);
    final var service = service(listBatches, currentProfileRepository, targetCandidates);
    when(listBatches.list(any())).thenReturn(List.of(batch));
    when(currentProfileRepository.findByBatchIds(Set.of(batchId)))
        .thenReturn(List.of(currentProfile(measurement(batchId), batch.currentMaterialStateId())));
    when(targetCandidates.query(any())).thenReturn(new FeedstockBatchProfileTargetCandidates(
        ProfileTargetAnalysisScenario.BLENDING_PLANNING,
        ProfileAxisNormalizerCode.EFFECTIVE_RANGE,
        List.of()
    ));

    final var view = service.query(queryWithTarget());

    assertThat(view.targetAnalysis()).isNotNull();
    assertThat(view.targetAnalysis().analysisScenario())
        .isEqualTo(ProfileTargetAnalysisScenario.BLENDING_PLANNING);
    assertThat(view.targetAnalysis().normalizerCode()).isEqualTo(ProfileAxisNormalizerCode.EFFECTIVE_RANGE);
  }

  @Test
  void omitsTargetCandidateCalculatedForAReplacedMaterialState() {
    final var batchId = FeedstockBatchId.newId();
    final var batch = batch(batchId);
    final var currentProfile = currentProfile(measurement(batchId), batch.currentMaterialStateId());
    final var listBatches = mock(ListFeedstockBatchesUseCase.class);
    final var currentProfileRepository = mock(FeedstockCurrentProfileQueryRepository.class);
    final var targetCandidates = mock(QueryFeedstockBatchProfileTargetCandidatesUseCase.class);
    final var service = service(listBatches, currentProfileRepository, targetCandidates);
    when(listBatches.list(any())).thenReturn(List.of(batch));
    when(currentProfileRepository.findByBatchIds(Set.of(batchId))).thenReturn(List.of(currentProfile));
    when(targetCandidates.query(any())).thenReturn(new FeedstockBatchProfileTargetCandidates(
        ProfileTargetAnalysisScenario.BLENDING_PLANNING,
        ProfileAxisNormalizerCode.EFFECTIVE_RANGE,
        List.of(new FeedstockBatchProfileTargetCandidate(
            batchId,
            MaterialStateId.newId(),
            currentProfile.evidence(),
            mock(ProfileTargetScoringInput.class),
            mock(ProfileTargetDistance.class),
            mock(CompositionContributionRoleAssessment.class)
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
            batch.currentMaterialStateId(),
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
    return new QueryFeedstockPlanningBatchesService(
        listBatches,
        new FeedstockCurrentProfileQueryService(currentProfileRepository),
        targetCandidates,
        new FeedstockCurrentProfileAssessmentService(
            graphEvaluator(),
            northAmericanProfileCalculator(),
            new PotentialAbvPredictor(
                FixedFinalGravityPredictionStrategy.dryCiderDefault(),
                new StandardGravityDropAbvPredictionStrategy()
            ),
            new FermentationStartReadinessAssessor(),
            new FeedstockCurrentProfileFreshnessAssessor(),
            new FeedstockPlanningReadinessAssessor(PhPlanningGuidancePolicy.curatedCiderV1()),
            new FeedstockProfileDataQualityAssessor()
        )
    );
  }

  private static FeedstockPlanningBatchViewQuery queryWithoutTarget() {
    return new FeedstockPlanningBatchViewQuery(
        WORKSPACE_ID,
        SEASON,
        null,
        null,
        ProfileTargetObjectiveWeights.balanced(),
        ProfileTargetAnalysisScenario.BLENDING_PLANNING,
        200
    );
  }

  private static FeedstockPlanningBatchViewQuery queryWithTarget() {
    return new FeedstockPlanningBatchViewQuery(
        WORKSPACE_ID,
        SEASON,
        new PredefinedFeedstockProfileTargetSelection(JolicoeurSugarAcidityZone.PERFECT_APPLES),
        null,
        ProfileTargetObjectiveWeights.balanced(),
        ProfileTargetAnalysisScenario.BLENDING_PLANNING,
        200
    );
  }

  private static FeedstockBatchSummary batch(FeedstockBatchId batchId) {
    return new FeedstockBatchSummary(
        batchId,
        WORKSPACE_ID,
        BatchName.of("Late season juice"),
        Vessel.named("Tank A-01"),
        FeedstockType.JUICE,
        SEASON,
        null,
        BatchLifecycle.ACTIVE,
        VolumeValue.ofLiters("120"),
        MaterialStateId.newId(),
        null
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
