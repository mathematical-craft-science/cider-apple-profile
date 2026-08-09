package org.craft.mcs.cap.feedstock.application.detail.usecase;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchOwnershipMismatchException;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.batch.usecase.FeedstockBatchViewEnricher;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileSummaryView;
import org.craft.mcs.cap.core.feedstock.application.vessel.port.out.FeedstockVesselReferenceQuery;
import org.craft.mcs.cap.shared.application.measurement.MeasurementFreshnessStatus;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileFreshnessView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileAssessmentView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockPlanningReadinessView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockPlanningReadinessStatus;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.service.FeedstockCurrentProfileFreshnessAssessor;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.service.FeedstockCurrentProfileAssessmentService;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.service.FeedstockPlanningReadinessAssessor;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.service.FeedstockProfileDataQualityAssessor;
import org.craft.mcs.cap.core.feedstock.application.detail.model.FeedstockBatchCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.application.detail.port.in.GetFeedstockBatchDetailQuery;
import org.craft.mcs.cap.core.feedstock.application.detail.usecase.GetFeedstockBatchDetailService;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.query.FeedstockCurrentProfileQueryService;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.port.out.FeedstockCurrentProfileQueryRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.testsupport.VesselTestFixtures;
import org.craft.mcs.cap.shared.testing.FeedstockMeasurementFixture;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetName;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.FixedFinalGravityPredictionStrategy;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.StandardGravityDropAbvPredictionStrategy;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessAssessor;
import org.craft.mcs.cap.core.profileanalysis.ph.domain.PhPlanningGuidancePolicy;
import org.craft.mcs.cap.shared.testing.ProfileCharacteristicsFixture;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures
    .graphEvaluator;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures
    .northAmericanProfileCalculator;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.testing.FeedstockCurrentProfileViews.currentProfile;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementRecordedAt;
import static java.util.Objects.requireNonNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;


class GetFeedstockBatchDetailServiceTest {

  private static final WorkspaceId WORKSPACE_ID = WorkspaceId.of(
      UUID.fromString("7c18963c-26d7-45c0-af2c-07998f4f6d6b")
  );
  private static GetFeedstockBatchDetailService service(
      FeedstockBatchRepository batchRepository,
      FeedstockCurrentProfileQueryRepository currentProfileRepository
  ) {
    final var vesselReferenceQuery = mock(FeedstockVesselReferenceQuery.class);
    when(vesselReferenceQuery.resolveReferences(any(), anySet())).thenReturn(java.util.Map.of(
        VesselTestFixtures.FEEDSTOCK_VESSEL_ID,
        VesselTestFixtures.reference(VesselTestFixtures.FEEDSTOCK_VESSEL_ID, "Tank A-01")
    ));
    return new GetFeedstockBatchDetailService(
        batchRepository,
        mock(FeedstockAccessControl.class),
        new FeedstockCurrentProfileQueryService(currentProfileRepository),
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
        ),
        new FeedstockBatchViewEnricher(vesselReferenceQuery)
    );
  }

  private static FeedstockBatch batch(
      FeedstockBatchId batchId,
      WorkspaceId workspaceId
  ) {
    return FeedstockBatch.create(
        batchId,
        workspaceId,
        BatchName.of("Late season juice"),
        VesselTestFixtures.vesselId("Tank A-01"),
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-09-01"), LocalDate.parse("2026-12-31")),
        VolumeValue.ofLiters("120")
    );
  }

  private static FeedstockMeasurementFixture measurement(
      FeedstockBatchId batchId,
      String name,
      String measuredAt
  ) {
    return FeedstockMeasurementFixture.create(
        MeasurementSetId.newId(),
        batchId,
        MeasurementSetName.of(name),
        ProfileCharacteristicsFixture.builder()
            .specificGravity(PointCharacteristicValue.of(SpecificGravityValue.of("1.060")))
            .titratableAcidity(PointCharacteristicValue.of(TitratableAcidityValue.of("5.0")))
            .ph(PointCharacteristicValue.of(PhValue.of("3.40")))
            .tannin(PointCharacteristicValue.of(TanninValue.of("1400")))
            .yeastAssimilableNitrogen(PointCharacteristicValue.of(YeastAssimilableNitrogenValue.of("120")))
            .build(),
        Instant.parse(measuredAt),
        feedstockMeasurementRecordedAt()
    );
  }

  @Test
  void returnsDetailReadModelWithCompositeCurrentProfile() {
    final var batchId = FeedstockBatchId.newId();
    final var batch = batch(batchId, WORKSPACE_ID);
    final var currentMeasurement = measurement(batchId, "Current profile", "2026-05-01T10:00:00Z");
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var currentProfileRepository = mock(FeedstockCurrentProfileQueryRepository.class);
    final var service = service(batchRepository, currentProfileRepository);
    final var stateAwareProfile = currentProfile(
        currentMeasurement,
        batch.currentMaterialStateId(),
        MeasurementFreshnessStatus.STALE
    );
    final var compositeTitratableAcidity =
        PointCharacteristicValue.of(TitratableAcidityValue.of("8.5"));
    when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));
    when(currentProfileRepository.findByBatchId(batchId))
        .thenReturn(Optional.of(new FeedstockCurrentProfileView(
            stateAwareProfile.batchId(),
            stateAwareProfile.currentMaterialStateId(),
            stateAwareProfile.specificGravity(),
            compositeTitratableAcidity,
            stateAwareProfile.ph(),
            stateAwareProfile.tannin(),
            stateAwareProfile.yeastAssimilableNitrogen(),
            stateAwareProfile.evidence(),
            stateAwareProfile.latestMeasurementSet()
        )));

    final var detail = service.get(new GetFeedstockBatchDetailQuery(batchId, WORKSPACE_ID));

    assertThat(detail)
        .returns(batchId, view -> view.batch().batch().batchId())
        .satisfies(view -> assertThat(view.currentProfile())
            .returns(
                FeedstockPlanningReadinessStatus.READY,
                profile -> profile.currentProfileAssessment().planningReadiness().status()
            )
            .satisfies(profile -> {
              final var summary = requireNonNull(profile.currentProfileSummary());
              assertThat(summary)
                  .returns(
                      compositeTitratableAcidity,
                      FeedstockCurrentProfileSummaryView::titratableAcidity
                  )
                  .returns(
                      stateAwareProfile.specificGravity(),
                      FeedstockCurrentProfileSummaryView::specificGravity
                  );
            })
            .satisfies(profile -> {
              final var freshness = requireNonNull(profile.currentProfileAssessment().freshness());
              assertThat(freshness)
                  .returns(
                      MeasurementFreshnessStatus.STALE,
                      FeedstockCurrentProfileFreshnessView::status
                  );
            })
            .satisfies(profile -> {
              final var graphEvaluation = requireNonNull(
                  profile.currentProfileAssessment().graphEvaluation()
              );
              assertThat(graphEvaluation.plottable()).isTrue();
            })
            .satisfies(profile -> assertThat(profile.currentProfileAssessment().potentialAbv()).isNotNull())
            .satisfies(profile -> assertThat(
                requireNonNull(profile.currentProfileSummary()).yeastAssimilableNitrogen()
            ).isNotNull())
            .satisfies(profile -> assertThat(
                profile.currentProfileAssessment().fermentationStartReadiness()
            ).isNotNull()));
  }

  @Test
  void returnsBlockedCurrentProfileWhenBatchHasNoMeasurements() {
    final var batchId = FeedstockBatchId.newId();
    final var batch = batch(batchId, WORKSPACE_ID);
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var currentProfileRepository = mock(FeedstockCurrentProfileQueryRepository.class);
    final var service = service(batchRepository, currentProfileRepository);
    when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));
    when(currentProfileRepository.findByBatchId(batchId)).thenReturn(Optional.empty());

    final var detail = service.get(new GetFeedstockBatchDetailQuery(batchId, WORKSPACE_ID));

    assertThat(detail.currentProfile())
        .returns(null, FeedstockBatchCurrentProfileView::currentProfileSummary)
        .extracting(FeedstockBatchCurrentProfileView::currentProfileAssessment)
        .returns(null, FeedstockCurrentProfileAssessmentView::freshness)
        .returns(null, FeedstockCurrentProfileAssessmentView::graphEvaluation)
        .returns(null, FeedstockCurrentProfileAssessmentView::potentialAbv)
        .returns(null, FeedstockCurrentProfileAssessmentView::fermentationStartReadiness)
        .returns(
            FeedstockPlanningReadinessStatus.BLOCKED,
            assessment -> assessment.planningReadiness().status()
        );
  }

  @Test
  void omitsCurrentProfileCalculatedForAReplacedMaterialState() {
    final var batchId = FeedstockBatchId.newId();
    final var batch = batch(batchId, WORKSPACE_ID);
    final var measurement = measurement(batchId, "Replaced state profile", "2026-05-01T10:00:00Z");
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var currentProfileRepository = mock(FeedstockCurrentProfileQueryRepository.class);
    final var service = service(batchRepository, currentProfileRepository);
    when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));
    when(currentProfileRepository.findByBatchId(batchId))
        .thenReturn(Optional.of(currentProfile(measurement, MaterialStateId.newId())));

    final var detail = service.get(new GetFeedstockBatchDetailQuery(batchId, WORKSPACE_ID));

    assertThat(detail.currentProfile())
        .returns(null, FeedstockBatchCurrentProfileView::currentProfileSummary)
        .extracting(FeedstockBatchCurrentProfileView::currentProfileAssessment)
        .extracting(FeedstockCurrentProfileAssessmentView::planningReadiness)
        .returns(
            FeedstockPlanningReadinessStatus.BLOCKED,
            FeedstockPlanningReadinessView::status
        );
  }

  @Test
  void rejectsBatchOwnedByAnotherWorkspace() {
    final var batchId = FeedstockBatchId.newId();
    final var actualOwner = WorkspaceId.of(UUID.fromString("14795a85-df62-4515-8fb0-d79ff274b920"));
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var currentProfileRepository = mock(FeedstockCurrentProfileQueryRepository.class);
    final var service = service(batchRepository, currentProfileRepository);
    when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch(batchId, actualOwner)));

    assertThatThrownBy(() -> service.get(new GetFeedstockBatchDetailQuery(batchId, WORKSPACE_ID)))
        .isInstanceOf(FeedstockBatchOwnershipMismatchException.class);
    verifyNoInteractions(currentProfileRepository);
  }

  @Test
  void rejectsMissingBatch() {
    final var batchId = FeedstockBatchId.newId();
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var currentProfileRepository = mock(FeedstockCurrentProfileQueryRepository.class);
    final var service = service(batchRepository, currentProfileRepository);
    when(batchRepository.findById(batchId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.get(new GetFeedstockBatchDetailQuery(batchId, WORKSPACE_ID)))
        .isInstanceOf(FeedstockBatchNotFoundException.class);
    verifyNoInteractions(currentProfileRepository);
  }
}
