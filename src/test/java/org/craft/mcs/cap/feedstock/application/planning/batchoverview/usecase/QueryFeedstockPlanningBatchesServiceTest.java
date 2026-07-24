package org.craft.mcs.cap.feedstock.application.planning.batchoverview.usecase;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentMeasurementFreshnessView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileDataQualityView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockPlanningReadinessView;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.FixedFinalGravityPredictionStrategy;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.StandardGravityDropAbvPredictionStrategy;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessAssessor;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessStatus;
import org.craft.mcs.cap.core.profileanalysis.ph.domain.PhPlanningGuidancePolicy;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileProjector;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.BuiltInSugarAcidityZoneProfiles;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.RectangleSugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockBatchProfileTargetCandidates;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.ListFeedstockBatchesUseCase;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.PredefinedFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.QueryFeedstockBatchProfileTargetCandidatesUseCase;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in.FeedstockPlanningBatchViewQuery;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileDataQualityStatus;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentMeasurementFreshnessStatus;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockPlanningReadinessStatus;
import org.craft.mcs.cap.core.feedstock.application.measurement.query.FeedstockBatchCurrentMeasurementQueryService;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.out.FeedstockMeasurementQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.usecase.FeedstockCurrentProfileAssessmentService;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.usecase.FeedstockPlanningContributionRoleExplainer;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileSettings;
import org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.usecase.QueryFeedstockPlanningBatchesService;
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
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementRecordedAt;
import static org.craft.mcs.cap.shared.testing.FeedstockMeasurementViews.view;
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
  private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-05-10T10:00:00Z"), ZoneOffset.UTC);

  @Test
  void returnsPlanningReadModelForBatchWithCurrentMeasurement() {
    final var batchId = FeedstockBatchId.newId();
    final var batch = batch(batchId);
    final var measurement = measurement(batchId);
    final var listBatches = mock(ListFeedstockBatchesUseCase.class);
    final var measurementRepository = mock(FeedstockMeasurementQueryRepository.class);
    final var targetCandidates = mock(QueryFeedstockBatchProfileTargetCandidatesUseCase.class);
    final var service = service(listBatches, measurementRepository, targetCandidates);
    when(listBatches.list(any())).thenReturn(List.of(batch));
    when(measurementRepository.findLatestByBatchIds(Set.of(batchId))).thenReturn(List.of(view(measurement)));

    final var view = service.query(queryWithoutTarget());

    assertThat(view.targetAnalysis()).isNull();
    assertThat(view.batches()).hasSize(1);
    final var row = view.batches().getFirst();
    assertThat(row.batch()).isEqualTo(batch);
    assertThat(row.currentMeasurement()).isNotNull();
    assertThat(row.measurementFreshness())
        .isNotNull()
        .extracting(FeedstockCurrentMeasurementFreshnessView::status)
        .isEqualTo(FeedstockCurrentMeasurementFreshnessStatus.CURRENT);
    assertThat(row.sugarAcidity()).isNotNull();
    assertThat(row.sugarAcidity().plottable()).isTrue();
    assertThat(row.sugarAcidity().point()).isPresent();
    assertThat(row.potentialAbv()).isNotNull();
    assertThat(row.yeastAssimilableNitrogen()).isNotNull();
    assertThat(row.yeastAssimilableNitrogen().axis()).isEqualTo(ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN);
    assertThat(row.planningReadiness())
        .isNotNull()
        .extracting(FeedstockPlanningReadinessView::status)
        .isEqualTo(FeedstockPlanningReadinessStatus.NEEDS_ATTENTION);
    assertThat(row.dataQuality())
        .isNotNull()
        .extracting(FeedstockCurrentProfileDataQualityView::status)
        .isEqualTo(FeedstockCurrentProfileDataQualityStatus.NEEDS_ATTENTION);
    assertThat(row.fermentationStartReadiness()).isNotNull();
    assertThat(row.fermentationStartReadiness().status()).isEqualTo(FermentationStartReadinessStatus.NEEDS_ATTENTION);
    assertThat(row.targetCandidate()).isNull();
    verifyNoInteractions(targetCandidates);
  }

  @Test
  void returnsTargetAnalysisMetadataWhenTargetIsRequested() {
    final var batchId = FeedstockBatchId.newId();
    final var listBatches = mock(ListFeedstockBatchesUseCase.class);
    final var measurementRepository = mock(FeedstockMeasurementQueryRepository.class);
    final var targetCandidates = mock(QueryFeedstockBatchProfileTargetCandidatesUseCase.class);
    final var service = service(listBatches, measurementRepository, targetCandidates);
    when(listBatches.list(any())).thenReturn(List.of(batch(batchId)));
    when(measurementRepository.findLatestByBatchIds(Set.of(batchId)))
        .thenReturn(List.of(view(measurement(batchId))));
    when(targetCandidates.query(any())).thenReturn(new FeedstockBatchProfileTargetCandidates(
        ProfileTargetScoringPurpose.BLENDING_PLANNING,
        ProfileAxisNormalizerCode.EFFECTIVE_RANGE,
        List.of()
    ));

    final var view = service.query(queryWithTarget());

    assertThat(view.targetAnalysis()).isNotNull();
    assertThat(view.targetAnalysis().purpose()).isEqualTo(ProfileTargetScoringPurpose.BLENDING_PLANNING);
    assertThat(view.targetAnalysis().normalizerCode()).isEqualTo(ProfileAxisNormalizerCode.EFFECTIVE_RANGE);
  }

  @Test
  void appliesWorkspaceMeasurementFreshnessSetting() {
    final var batchId = FeedstockBatchId.newId();
    final var batch = batch(batchId);
    final var measurement = measurement(batchId);
    final var listBatches = mock(ListFeedstockBatchesUseCase.class);
    final var measurementRepository = mock(FeedstockMeasurementQueryRepository.class);
    final var targetCandidates = mock(QueryFeedstockBatchProfileTargetCandidatesUseCase.class);
    final var service = service(
        listBatches,
        measurementRepository,
        targetCandidates,
        Duration.ofDays(3)
    );
    when(listBatches.list(any())).thenReturn(List.of(batch));
    when(measurementRepository.findLatestByBatchIds(Set.of(batchId))).thenReturn(List.of(view(measurement)));

    final var view = service.query(queryWithoutTarget());

    assertThat(view.batches())
        .singleElement()
        .satisfies(row -> assertThat(row.measurementFreshness())
            .isNotNull()
            .satisfies(freshness -> {
              assertThat(freshness.status()).isEqualTo(FeedstockCurrentMeasurementFreshnessStatus.STALE);
              assertThat(freshness.stale()).isTrue();
              assertThat(freshness.reason()).isEqualTo(
                  "Current measurement is older than configured freshness threshold"
              );
            }));
  }

  private static QueryFeedstockPlanningBatchesService service(
      ListFeedstockBatchesUseCase listBatches,
      FeedstockMeasurementQueryRepository measurementRepository,
      QueryFeedstockBatchProfileTargetCandidatesUseCase targetCandidates
  ) {
    return service(
        listBatches,
        measurementRepository,
        targetCandidates,
        Duration.ofDays(14)
    );
  }

  private static QueryFeedstockPlanningBatchesService service(
      ListFeedstockBatchesUseCase listBatches,
      FeedstockMeasurementQueryRepository measurementRepository,
      QueryFeedstockBatchProfileTargetCandidatesUseCase targetCandidates,
      Duration measurementStaleAfter
  ) {
    return new QueryFeedstockPlanningBatchesService(
        listBatches,
        new FeedstockBatchCurrentMeasurementQueryService(measurementRepository),
        targetCandidates,
        projector(),
        new PotentialAbvPredictor(
            FixedFinalGravityPredictionStrategy.dryCiderDefault(),
            new StandardGravityDropAbvPredictionStrategy()
        ),
        new FermentationStartReadinessAssessor(),
        new FeedstockCurrentProfileAssessmentService(
            CLOCK,
            PhPlanningGuidancePolicy.curatedCiderV1()
        ),
        _ -> new FeedstockCurrentProfileSettings(measurementStaleAfter),
        new FeedstockPlanningContributionRoleExplainer()
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

  private static FeedstockPlanningBatchViewQuery queryWithoutTarget() {
    return new FeedstockPlanningBatchViewQuery(
        WORKSPACE_ID,
        SEASON,
        null,
        null,
        ProfileTargetObjectiveWeights.balanced(),
        ProfileTargetScoringPurpose.BLENDING_PLANNING,
        200
    );
  }

  private static FeedstockPlanningBatchViewQuery queryWithTarget() {
    return new FeedstockPlanningBatchViewQuery(
        WORKSPACE_ID,
        SEASON,
        new PredefinedFeedstockProfileTargetSelection(SugarAcidityZone.PERFECT_APPLES),
        null,
        ProfileTargetObjectiveWeights.balanced(),
        ProfileTargetScoringPurpose.BLENDING_PLANNING,
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
        VolumeValue.ofLiters("120"),
        null
    );
  }

  private static FeedstockMeasurementFixture measurement(FeedstockBatchId batchId) {
    return FeedstockMeasurementFixture.create(
        FeedstockMeasurementId.newId(),
        batchId,
        FeedstockMeasurementName.of("Current profile"),
        ChemicalProfile.builder()
            .sugar(PointCharacteristicValue.of(SugarConcentration.ofSpecificGravity(SpecificGravityValue.of("1.060"))))
            .titratableAcidity(PointCharacteristicValue.of(TitratableAcidityValue.of("5.0")))
            .yeastAssimilableNitrogen(PointCharacteristicValue.of(YeastAssimilableNitrogenValue.of("120")))
            .build(),
        Instant.parse("2026-05-01T10:00:00Z"),
        feedstockMeasurementRecordedAt()
    );
  }
}
