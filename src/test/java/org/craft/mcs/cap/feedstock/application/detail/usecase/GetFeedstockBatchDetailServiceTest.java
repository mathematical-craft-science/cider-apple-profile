package org.craft.mcs.cap.feedstock.application.detail.usecase;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchOwnershipMismatchException;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.port.in.FeedstockCurrentMeasurementFreshnessStatus;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.port.in.FeedstockCurrentMeasurementFreshnessView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.port.in.FeedstockPlanningReadinessStatus;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileSettings;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.usecase.FeedstockCurrentProfileAssessmentService;
import org.craft.mcs.cap.core.feedstock.application.detail.port.in.FeedstockBatchCurrentProfileOperationType;
import org.craft.mcs.cap.core.feedstock.application.detail.port.in.FeedstockBatchCurrentProfileOperationView;
import org.craft.mcs.cap.core.feedstock.application.detail.port.in.FeedstockBatchCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.application.detail.port.in.GetFeedstockBatchDetailQuery;
import org.craft.mcs.cap.core.feedstock.application.detail.usecase.GetFeedstockBatchDetailService;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.in.FeedstockMeasurementSummary;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.in.ListFeedstockMeasurementsUseCase;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.out.FeedstockMeasurementQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.measurement.query.FeedstockBatchCurrentMeasurementQueryService;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockBatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockVessel;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.FixedFinalGravityPredictionStrategy;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.StandardGravityDropAbvPredictionStrategy;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessAssessor;
import org.craft.mcs.cap.core.profileanalysis.ph.domain.PhPlanningGuidancePolicy;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileEvaluation;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileProjector;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.BuiltInSugarAcidityZoneProfiles;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.RectangleSugarAcidityZoneAssessor;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static java.util.Objects.requireNonNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


class GetFeedstockBatchDetailServiceTest {

  private static final WorkspaceId WORKSPACE_ID = WorkspaceId.of(
      UUID.fromString("7c18963c-26d7-45c0-af2c-07998f4f6d6b")
  );
  private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-05-10T10:00:00Z"), ZoneOffset.UTC);

  private static GetFeedstockBatchDetailService service(
      FeedstockBatchRepository batchRepository,
      ListFeedstockMeasurementsUseCase listMeasurements,
      FeedstockMeasurementQueryRepository measurementRepository
  ) {
    return new GetFeedstockBatchDetailService(
        batchRepository,
        mock(FeedstockAccessControl.class),
        listMeasurements,
        new FeedstockBatchCurrentMeasurementQueryService(measurementRepository),
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
        _ -> new FeedstockCurrentProfileSettings(Duration.ofDays(3))
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

  private static FeedstockBatch batch(
      FeedstockBatchId batchId,
      WorkspaceId workspaceId
  ) {
    return FeedstockBatch.create(
        batchId,
        workspaceId,
        FeedstockBatchName.of("Late season juice"),
        FeedstockVessel.of("Tank A-01"),
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-09-01"), LocalDate.parse("2026-12-31")),
        VolumeValue.ofLiters("120")
    );
  }

  private static FeedstockMeasurement measurement(
      FeedstockBatchId batchId,
      String name,
      String measuredAt
  ) {
    return FeedstockMeasurement.create(
        FeedstockMeasurementId.newId(),
        batchId,
        FeedstockMeasurementName.of(name),
        ChemicalProfile.builder()
            .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(SpecificGravityValue.of("1.060"))))
            .titratableAcidity(ExactMeasurement.of(TitratableAcidityValue.of("5.0")))
            .ph(ExactMeasurement.of(PhValue.of("3.40")))
            .tanninity(ExactMeasurement.of(TanninityValue.of("1400")))
            .yeastAssimilableNitrogen(ExactMeasurement.of(YeastAssimilableNitrogenValue.of("120")))
            .build(),
        Instant.parse(measuredAt)
    );
  }

  private static FeedstockMeasurementSummary measurementSummary(
      FeedstockBatch batch,
      FeedstockMeasurement measurement
  ) {
    return new FeedstockMeasurementSummary(
        measurement.measurementId(),
        batch.workspaceId(),
        measurement.batchId(),
        measurement.name(),
        measurement.measuredAt(),
        measurement.chemicalProfile(),
        measurement.note()
    );
  }

  @Test
  void returnsDetailReadModelWithCurrentProfileAndMeasurementHistory() {
    final var batchId = FeedstockBatchId.newId();
    final var batch = batch(batchId, WORKSPACE_ID);
    final var currentMeasurement = measurement(batchId, "Current profile", "2026-05-01T10:00:00Z");
    final var olderMeasurement = measurement(batchId, "Older profile", "2026-04-20T10:00:00Z");
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var listMeasurements = mock(ListFeedstockMeasurementsUseCase.class);
    final var measurementRepository = mock(FeedstockMeasurementQueryRepository.class);
    final var service = service(batchRepository, listMeasurements, measurementRepository);
    when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));
    when(measurementRepository.findLatestByBatchId(batchId)).thenReturn(Optional.of(currentMeasurement));
    when(listMeasurements.listByBatchId(batchId)).thenReturn(List.of(
        measurementSummary(batch, currentMeasurement),
        measurementSummary(batch, olderMeasurement)
    ));

    final var detail = service.get(new GetFeedstockBatchDetailQuery(batchId, WORKSPACE_ID));

    assertThat(detail)
        .returns(batchId, view -> view.batch().batchId())
        .satisfies(view -> assertThat(view.measurements())
            .extracting(summary -> summary.name().value())
            .containsExactly("Current profile", "Older profile"))
        .satisfies(view -> assertThat(view.currentProfile())
            .returns(FeedstockPlanningReadinessStatus.READY, profile -> profile.planningReadiness().status())
            .satisfies(profile -> {
              final var latestMeasurement = requireNonNull(profile.latestMeasurement());
              assertThat(latestMeasurement)
                  .returns(currentMeasurement.measurementId(), FeedstockMeasurementSummary::measurementId);
            })
            .satisfies(profile -> {
              final var freshness = requireNonNull(profile.measurementFreshness());
              assertThat(freshness)
                  .returns(
                      FeedstockCurrentMeasurementFreshnessStatus.STALE,
                      FeedstockCurrentMeasurementFreshnessView::status
                  );
            })
            .satisfies(profile -> {
              final var sugarAcidity = requireNonNull(profile.sugarAcidity());
              assertThat(sugarAcidity)
                  .returns(true, SugarAcidityProfileEvaluation::plottable);
            })
            .satisfies(profile -> assertThat(profile.potentialAbv()).isNotNull())
            .satisfies(profile -> assertThat(profile.yeastAssimilableNitrogen()).isNotNull())
            .satisfies(profile -> assertThat(profile.fermentationStartReadiness()).isNotNull())
            .satisfies(profile -> {
              final var lastOperation = requireNonNull(profile.lastOperation());
              assertThat(lastOperation)
                  .returns(
                      FeedstockBatchCurrentProfileOperationType.MEASURED,
                      FeedstockBatchCurrentProfileOperationView::type
                  )
                  .returns(currentMeasurement.measuredAt(), FeedstockBatchCurrentProfileOperationView::occurredAt);
            }));
  }

  @Test
  void returnsBlockedCurrentProfileWhenBatchHasNoMeasurements() {
    final var batchId = FeedstockBatchId.newId();
    final var batch = batch(batchId, WORKSPACE_ID);
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var listMeasurements = mock(ListFeedstockMeasurementsUseCase.class);
    final var measurementRepository = mock(FeedstockMeasurementQueryRepository.class);
    final var service = service(batchRepository, listMeasurements, measurementRepository);
    when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));
    when(measurementRepository.findLatestByBatchId(batchId)).thenReturn(Optional.empty());
    when(listMeasurements.listByBatchId(batchId)).thenReturn(List.of());

    final var detail = service.get(new GetFeedstockBatchDetailQuery(batchId, WORKSPACE_ID));

    assertThat(detail.currentProfile())
        .returns(null, FeedstockBatchCurrentProfileView::latestMeasurement)
        .returns(null, FeedstockBatchCurrentProfileView::measurementFreshness)
        .returns(null, FeedstockBatchCurrentProfileView::sugarAcidity)
        .returns(null, FeedstockBatchCurrentProfileView::potentialAbv)
        .returns(null, FeedstockBatchCurrentProfileView::fermentationStartReadiness)
        .returns(FeedstockPlanningReadinessStatus.BLOCKED, profile -> profile.planningReadiness().status());
    assertThat(detail.measurements()).isEmpty();
  }

  @Test
  void rejectsBatchOwnedByAnotherWorkspace() {
    final var batchId = FeedstockBatchId.newId();
    final var actualOwner = WorkspaceId.of(UUID.fromString("14795a85-df62-4515-8fb0-d79ff274b920"));
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var listMeasurements = mock(ListFeedstockMeasurementsUseCase.class);
    final var measurementRepository = mock(FeedstockMeasurementQueryRepository.class);
    final var service = service(batchRepository, listMeasurements, measurementRepository);
    when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch(batchId, actualOwner)));

    assertThatThrownBy(() -> service.get(new GetFeedstockBatchDetailQuery(batchId, WORKSPACE_ID)))
        .isInstanceOf(FeedstockBatchOwnershipMismatchException.class);
    verifyNoInteractions(listMeasurements, measurementRepository);
  }

  @Test
  void rejectsMissingBatch() {
    final var batchId = FeedstockBatchId.newId();
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var listMeasurements = mock(ListFeedstockMeasurementsUseCase.class);
    final var measurementRepository = mock(FeedstockMeasurementQueryRepository.class);
    final var service = service(batchRepository, listMeasurements, measurementRepository);
    when(batchRepository.findById(batchId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.get(new GetFeedstockBatchDetailQuery(batchId, WORKSPACE_ID)))
        .isInstanceOf(FeedstockBatchNotFoundException.class);
    verifyNoInteractions(listMeasurements, measurementRepository);
  }
}
