package org.craft.mcs.cap.feedstock.application.detail.usecase;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchOwnershipMismatchException;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.shared.application.measurement.MeasurementFreshnessStatus;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileFreshnessView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileAssessmentView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockPlanningReadinessView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockPlanningReadinessStatus;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockLatestMeasurementView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.service.FeedstockCurrentProfileFreshnessAssessor;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.service.FeedstockCurrentProfileAssessmentService;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.service.FeedstockPlanningReadinessAssessor;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.service.FeedstockProfileDataQualityAssessor;
import org.craft.mcs.cap.core.feedstock.application.detail.model.FeedstockBatchCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.application.detail.port.in.GetFeedstockBatchDetailQuery;
import org.craft.mcs.cap.core.feedstock.application.detail.usecase.GetFeedstockBatchDetailService;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementSummary;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.in.ListFeedstockMeasurementsUseCase;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.query.FeedstockCurrentProfileQueryService;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.port.out.FeedstockCurrentProfileQueryRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.shared.kernel.material.value.Vessel;
import org.craft.mcs.cap.shared.testing.FeedstockMeasurementFixture;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementName;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.FixedFinalGravityPredictionStrategy;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.StandardGravityDropAbvPredictionStrategy;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessAssessor;
import org.craft.mcs.cap.core.profileanalysis.ph.domain.PhPlanningGuidancePolicy;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.core.profileanalysis.compatibility.application.ChemicalProfileAssessment;
import org.craft.mcs.cap.core.profileanalysis.compatibility.application.ChemicalProfileAssessmentAdapter;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
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
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures
    .chemicalProfileAssessmentAdapter;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.testing.FeedstockMeasurementViews.currentProfile;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementRecordedAt;
import static java.util.Objects.requireNonNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


class GetFeedstockBatchDetailServiceTest {

  private static final WorkspaceId WORKSPACE_ID = WorkspaceId.of(
      UUID.fromString("7c18963c-26d7-45c0-af2c-07998f4f6d6b")
  );
  private static GetFeedstockBatchDetailService service(
      FeedstockBatchRepository batchRepository,
      ListFeedstockMeasurementsUseCase listMeasurements,
      FeedstockCurrentProfileQueryRepository currentProfileRepository
  ) {
    return new GetFeedstockBatchDetailService(
        batchRepository,
        mock(FeedstockAccessControl.class),
        listMeasurements,
        new FeedstockCurrentProfileQueryService(currentProfileRepository),
        new FeedstockCurrentProfileAssessmentService(
            projector(),
            new ChemicalProfileCapabilityAssessor(),
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

  private static ChemicalProfileAssessmentAdapter projector() {
    return chemicalProfileAssessmentAdapter();
  }

  private static FeedstockBatch batch(
      FeedstockBatchId batchId,
      WorkspaceId workspaceId
  ) {
    return FeedstockBatch.create(
        batchId,
        workspaceId,
        BatchName.of("Late season juice"),
        Vessel.named("Tank A-01"),
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
        FeedstockMeasurementId.newId(),
        batchId,
        FeedstockMeasurementName.of(name),
        ChemicalProfile.builder()
            .sugar(PointCharacteristicValue.of(SugarConcentration.ofSpecificGravity(SpecificGravityValue.of("1.060"))))
            .titratableAcidity(PointCharacteristicValue.of(TitratableAcidityValue.of("5.0")))
            .ph(PointCharacteristicValue.of(PhValue.of("3.40")))
            .tannin(PointCharacteristicValue.of(TanninValue.of("1400")))
            .yeastAssimilableNitrogen(PointCharacteristicValue.of(YeastAssimilableNitrogenValue.of("120")))
            .build(),
        Instant.parse(measuredAt),
        feedstockMeasurementRecordedAt()
    );
  }

  private static FeedstockMeasurementSummary measurementSummary(
      FeedstockBatch batch,
      FeedstockMeasurementFixture measurement
  ) {
    return new FeedstockMeasurementSummary(
        measurement.measurementId(),
        batch.workspaceId(),
        measurement.batchId(),
        measurement.name(),
        measurement.measuredAt(),
        0,
        measurement.chemicalProfile(),
        measurement.note()
    );
  }

  @Test
  void returnsDetailReadModelWithCurrentProfileAndMeasurementHistory() {
    final var batchId = FeedstockBatchId.newId();
    final var batch = batch(batchId, WORKSPACE_ID);
    final var currentMeasurement = measurement(batchId, "Current profile", "2026-05-01T10:00:00Z");
    final var newerNonContributingMeasurement = measurement(
        batchId,
        "Newer non-contributing profile",
        "2026-05-02T10:00:00Z"
    );
    final var olderMeasurement = measurement(batchId, "Older profile", "2026-04-20T10:00:00Z");
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var listMeasurements = mock(ListFeedstockMeasurementsUseCase.class);
    final var currentProfileRepository = mock(FeedstockCurrentProfileQueryRepository.class);
    final var service = service(batchRepository, listMeasurements, currentProfileRepository);
    final var stateAwareProfile = currentProfile(
        currentMeasurement,
        batch.currentMaterialStateId(),
        MeasurementFreshnessStatus.STALE
    );
    final var compositeChemicalProfile = ChemicalProfile.builder()
        .sugar(currentMeasurement.chemicalProfile().sugar().orElseThrow())
        .titratableAcidity(PointCharacteristicValue.of(TitratableAcidityValue.of("8.5")))
        .ph(currentMeasurement.chemicalProfile().ph().orElseThrow())
        .tannin(currentMeasurement.chemicalProfile().tannin().orElseThrow())
        .yeastAssimilableNitrogen(
            currentMeasurement.chemicalProfile().yeastAssimilableNitrogen().orElseThrow()
        )
        .build();
    when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));
    when(currentProfileRepository.findByBatchId(batchId))
        .thenReturn(Optional.of(new FeedstockCurrentProfileView(
            stateAwareProfile.batchId(),
            stateAwareProfile.currentMaterialStateId(),
            compositeChemicalProfile,
            stateAwareProfile.evidence(),
            stateAwareProfile.latestMeasurement()
        )));
    when(listMeasurements.listByBatchId(batchId)).thenReturn(List.of(
        measurementSummary(batch, newerNonContributingMeasurement),
        measurementSummary(batch, currentMeasurement),
        measurementSummary(batch, olderMeasurement)
    ));

    final var detail = service.get(new GetFeedstockBatchDetailQuery(batchId, WORKSPACE_ID));

    assertThat(detail)
        .returns(batchId, view -> view.batch().batchId())
        .satisfies(view -> assertThat(view.measurements())
            .extracting(summary -> summary.name().value())
            .containsExactly(
                "Newer non-contributing profile",
                "Current profile",
                "Older profile"
            ))
        .satisfies(view -> assertThat(view.currentProfile())
            .returns(
                FeedstockPlanningReadinessStatus.READY,
                profile -> profile.currentProfileAssessment().planningReadiness().status()
            )
            .satisfies(profile -> {
              final var latestMeasurement = requireNonNull(profile.latestMeasurement());
              assertThat(latestMeasurement)
                  .returns(currentMeasurement.measurementId(), FeedstockMeasurementSummary::measurementId);
            })
            .satisfies(profile -> {
              final var summary = requireNonNull(profile.currentProfileSummary());
              assertThat(summary.chemicalProfile()).isSameAs(compositeChemicalProfile);
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
              final var profileAssessment = requireNonNull(
                  profile.currentProfileAssessment().profileAssessment()
              );
              assertThat(profileAssessment)
                  .returns(true, ChemicalProfileAssessment::plottable);
            })
            .satisfies(profile -> assertThat(profile.currentProfileAssessment().potentialAbv()).isNotNull())
            .satisfies(profile -> assertThat(
                profile.currentProfileAssessment().yeastAssimilableNitrogen()
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
    final var listMeasurements = mock(ListFeedstockMeasurementsUseCase.class);
    final var currentProfileRepository = mock(FeedstockCurrentProfileQueryRepository.class);
    final var service = service(batchRepository, listMeasurements, currentProfileRepository);
    when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));
    when(currentProfileRepository.findByBatchId(batchId)).thenReturn(Optional.empty());
    when(listMeasurements.listByBatchId(batchId)).thenReturn(List.of());

    final var detail = service.get(new GetFeedstockBatchDetailQuery(batchId, WORKSPACE_ID));

    assertThat(detail.currentProfile())
        .returns(null, FeedstockBatchCurrentProfileView::latestMeasurement)
        .returns(null, FeedstockBatchCurrentProfileView::currentProfileSummary)
        .extracting(FeedstockBatchCurrentProfileView::currentProfileAssessment)
        .returns(null, FeedstockCurrentProfileAssessmentView::freshness)
        .returns(null, FeedstockCurrentProfileAssessmentView::profileAssessment)
        .returns(null, FeedstockCurrentProfileAssessmentView::potentialAbv)
        .returns(null, FeedstockCurrentProfileAssessmentView::fermentationStartReadiness)
        .returns(
            FeedstockPlanningReadinessStatus.BLOCKED,
            assessment -> assessment.planningReadiness().status()
        );
    assertThat(detail.measurements()).isEmpty();
  }

  @Test
  void omitsCurrentProfileCalculatedForAReplacedMaterialState() {
    final var batchId = FeedstockBatchId.newId();
    final var batch = batch(batchId, WORKSPACE_ID);
    final var measurement = measurement(batchId, "Replaced state profile", "2026-05-01T10:00:00Z");
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var listMeasurements = mock(ListFeedstockMeasurementsUseCase.class);
    final var currentProfileRepository = mock(FeedstockCurrentProfileQueryRepository.class);
    final var service = service(batchRepository, listMeasurements, currentProfileRepository);
    when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));
    when(currentProfileRepository.findByBatchId(batchId))
        .thenReturn(Optional.of(currentProfile(measurement, MaterialStateId.newId())));
    when(listMeasurements.listByBatchId(batchId))
        .thenReturn(List.of(measurementSummary(batch, measurement)));

    final var detail = service.get(new GetFeedstockBatchDetailQuery(batchId, WORKSPACE_ID));

    assertThat(detail.currentProfile())
        .returns(null, FeedstockBatchCurrentProfileView::latestMeasurement)
        .returns(null, FeedstockBatchCurrentProfileView::currentProfileSummary)
        .extracting(FeedstockBatchCurrentProfileView::currentProfileAssessment)
        .extracting(FeedstockCurrentProfileAssessmentView::planningReadiness)
        .returns(
            FeedstockPlanningReadinessStatus.BLOCKED,
            FeedstockPlanningReadinessView::status
        );
  }

  @Test
  void doesNotCombineCurrentProfileWithAnOlderMeasurementRevision() {
    final var batchId = FeedstockBatchId.newId();
    final var batch = batch(batchId, WORKSPACE_ID);
    final var measurement = measurement(batchId, "Corrected profile", "2026-05-01T10:00:00Z");
    final var initialProfile = currentProfile(measurement, batch.currentMaterialStateId());
    final var correctedProfile = new FeedstockCurrentProfileView(
        initialProfile.batchId(),
        initialProfile.currentMaterialStateId(),
        initialProfile.chemicalProfile(),
        initialProfile.evidence(),
        new FeedstockLatestMeasurementView(
            measurement.measurementId(),
            measurement.name(),
            measurement.measuredAt(),
            1,
            measurement.note()
        )
    );
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var listMeasurements = mock(ListFeedstockMeasurementsUseCase.class);
    final var currentProfileRepository = mock(FeedstockCurrentProfileQueryRepository.class);
    final var service = service(batchRepository, listMeasurements, currentProfileRepository);
    when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));
    when(currentProfileRepository.findByBatchId(batchId)).thenReturn(Optional.of(correctedProfile));
    when(listMeasurements.listByBatchId(batchId))
        .thenReturn(List.of(measurementSummary(batch, measurement)));

    final var detail = service.get(new GetFeedstockBatchDetailQuery(batchId, WORKSPACE_ID));

    assertThat(detail.currentProfile())
        .returns(null, FeedstockBatchCurrentProfileView::latestMeasurement)
        .extracting(FeedstockBatchCurrentProfileView::currentProfileSummary)
        .isNotNull();
  }

  @Test
  void rejectsBatchOwnedByAnotherWorkspace() {
    final var batchId = FeedstockBatchId.newId();
    final var actualOwner = WorkspaceId.of(UUID.fromString("14795a85-df62-4515-8fb0-d79ff274b920"));
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var listMeasurements = mock(ListFeedstockMeasurementsUseCase.class);
    final var currentProfileRepository = mock(FeedstockCurrentProfileQueryRepository.class);
    final var service = service(batchRepository, listMeasurements, currentProfileRepository);
    when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch(batchId, actualOwner)));

    assertThatThrownBy(() -> service.get(new GetFeedstockBatchDetailQuery(batchId, WORKSPACE_ID)))
        .isInstanceOf(FeedstockBatchOwnershipMismatchException.class);
    verifyNoInteractions(listMeasurements, currentProfileRepository);
  }

  @Test
  void rejectsMissingBatch() {
    final var batchId = FeedstockBatchId.newId();
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var listMeasurements = mock(ListFeedstockMeasurementsUseCase.class);
    final var currentProfileRepository = mock(FeedstockCurrentProfileQueryRepository.class);
    final var service = service(batchRepository, listMeasurements, currentProfileRepository);
    when(batchRepository.findById(batchId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.get(new GetFeedstockBatchDetailQuery(batchId, WORKSPACE_ID)))
        .isInstanceOf(FeedstockBatchNotFoundException.class);
    verifyNoInteractions(listMeasurements, currentProfileRepository);
  }
}
