package org.craft.mcs.cap.feedstock.application.workspace.usecase;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.craft.mcs.cap.core.cideranalysis.abv.domain.FixedFinalGravityPredictionStrategy;
import org.craft.mcs.cap.core.cideranalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.cideranalysis.abv.domain.StandardGravityDropAbvPredictionStrategy;
import org.craft.mcs.cap.core.cideranalysis.fermentationstart.domain.FermentationStartReadinessAssessor;
import org.craft.mcs.cap.core.cideranalysis.fermentationstart.domain.FermentationStartReadinessStatus;
import org.craft.mcs.cap.core.cideranalysis.profileaxis.domain.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.projection.domain.SugarAcidityProfileProjector;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.BuiltInSugarAcidityZoneProfiles;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.RectangleSugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockBatchProfileTargetCandidates;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.ListFeedstockBatchesUseCase;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.PredefinedFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.QueryFeedstockBatchProfileTargetCandidatesUseCase;
import org.craft.mcs.cap.core.feedstock.application.workspace.port.in.FeedstockWorkspaceBatchViewQuery;
import org.craft.mcs.cap.core.feedstock.application.measurement.query.FeedstockBatchCurrentMeasurementQueryService;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.out.FeedstockMeasurementQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.workspace.usecase.QueryFeedstockWorkspaceBatchesService;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockBatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


class QueryFeedstockWorkspaceBatchesServiceTest {

  private static final CiderMakerId CIDER_MAKER_ID = CiderMakerId.of(UUID.fromString("7c18963c-26d7-45c0-af2c-07998f4f6d6b"));
  private static final FeedstockSeason SEASON = FeedstockSeason.of(
      LocalDate.of(2026, 9, 1),
      LocalDate.of(2026, 12, 31)
  );

  @Test
  void returnsScreenReadModelForBatchWithCurrentMeasurement() {
    final var batchId = FeedstockBatchId.newId();
    final var batch = batch(batchId);
    final var measurement = measurement(batchId);
    final var listBatches = mock(ListFeedstockBatchesUseCase.class);
    final var measurementRepository = mock(FeedstockMeasurementQueryRepository.class);
    final var targetCandidates = mock(QueryFeedstockBatchProfileTargetCandidatesUseCase.class);
    final var service = service(listBatches, measurementRepository, targetCandidates);
    when(listBatches.list(any())).thenReturn(List.of(batch));
    when(measurementRepository.findLatestByBatchIds(Set.of(batchId))).thenReturn(List.of(measurement));

    final var view = service.query(queryWithoutTarget());

    assertThat(view.targetAnalysis()).isNull();
    assertThat(view.batches()).hasSize(1);
    final var row = view.batches().getFirst();
    assertThat(row.batch()).isEqualTo(batch);
    assertThat(row.currentMeasurement()).isNotNull();
    assertThat(row.sugarAcidity()).isNotNull();
    assertThat(row.sugarAcidity().plottable()).isTrue();
    assertThat(row.sugarAcidity().point()).isPresent();
    assertThat(row.potentialAbv()).isNotNull();
    assertThat(row.yeastAssimilableNitrogen()).isNotNull();
    assertThat(row.yeastAssimilableNitrogen().axis()).isEqualTo(ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN);
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
    when(measurementRepository.findLatestByBatchIds(Set.of(batchId))).thenReturn(List.of(measurement(batchId)));
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

  private static QueryFeedstockWorkspaceBatchesService service(
      ListFeedstockBatchesUseCase listBatches,
      FeedstockMeasurementQueryRepository measurementRepository,
      QueryFeedstockBatchProfileTargetCandidatesUseCase targetCandidates
  ) {
    return new QueryFeedstockWorkspaceBatchesService(
        listBatches,
        new FeedstockBatchCurrentMeasurementQueryService(measurementRepository),
        targetCandidates,
        projector(),
        new PotentialAbvPredictor(
            FixedFinalGravityPredictionStrategy.dryCiderDefault(),
            new StandardGravityDropAbvPredictionStrategy()
        ),
        new FermentationStartReadinessAssessor()
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

  private static FeedstockWorkspaceBatchViewQuery queryWithoutTarget() {
    return new FeedstockWorkspaceBatchViewQuery(
        CIDER_MAKER_ID,
        SEASON,
        null,
        null,
        ProfileTargetObjectiveWeights.balanced(),
        ProfileTargetScoringPurpose.BLENDING_PLANNING,
        200
    );
  }

  private static FeedstockWorkspaceBatchViewQuery queryWithTarget() {
    return new FeedstockWorkspaceBatchViewQuery(
        CIDER_MAKER_ID,
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
        CIDER_MAKER_ID,
        FeedstockBatchName.of("Late season juice"),
        FeedstockType.APPLE_JUICE,
        SEASON,
        null,
        VolumeValue.ofLiters("120"),
        null
    );
  }

  private static FeedstockMeasurement measurement(FeedstockBatchId batchId) {
    return FeedstockMeasurement.create(
        FeedstockMeasurementId.newId(),
        batchId,
        FeedstockMeasurementName.of("Current profile"),
        ChemicalProfile.builder()
            .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(SpecificGravityValue.of("1.060"))))
            .titratableAcidity(ExactMeasurement.of(TitratableAcidityValue.of("5.0")))
            .yeastAssimilableNitrogen(ExactMeasurement.of(YeastAssimilableNitrogenValue.of("120")))
            .build(),
        Instant.parse("2026-05-01T10:00:00Z")
    );
  }
}
