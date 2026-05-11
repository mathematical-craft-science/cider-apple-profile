package org.craft.mcs.cap.feedstock.application.usecase;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.OptimizeChemicalProfileCompositionResult;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.OptimizeChemicalProfileCompositionUseCase;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.ChemicalProfileCompositionCalculatorCode;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.ChemicalProfileCompositionTanninPolicy;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.optimization.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.optimization.CompositionOptimizationMode;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.optimization.ChemicalProfileCompositionOptimizationResult;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.optimization.DiscretizedCompositionOptimizationSettings;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.TanninTargetRange;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.ZoneGeometrySugarAcidityTargetResolver;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone.BuiltInSugarAcidityZoneProfiles;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone.RectangleSugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone.SugarAcidityZone;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockMeasurementPointFilters;
import org.craft.mcs.cap.core.feedstock.application.port.in.OptimizeFeedstockBatchCompositionCommand;
import org.craft.mcs.cap.core.feedstock.application.port.in.PredefinedFeedstockBatchBlendProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockMeasurementQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.port.out.LatestFeedstockMeasurementSugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.core.feedstock.application.usecase.OptimizeFeedstockBatchCompositionService;
import org.craft.mcs.cap.core.feedstock.application.InsufficientFeedstockCompositionSourcesException;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockBatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class OptimizeFeedstockBatchCompositionServiceTest {

  @Test
  void buildsOptimizationProblemFromCurrentFeedstockBatchMeasurements() {
    final var measurementRepository = mock(FeedstockMeasurementQueryRepository.class);
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var optimizer = mockOptimizer();
    final var service = service(measurementRepository, batchRepository, optimizer);
    final var ciderMakerId = CiderMakerId.of(randomUUID());
    final var usableBatch = batch(ciderMakerId, "20");
    final var secondUsableBatch = batch(ciderMakerId, "30");
    final var incompleteBatch = batch(ciderMakerId, "10");
    final var usableMeasurement = sample(usableBatch.batchId(), profile("1.065", "5.5"));
    final var secondUsableMeasurement = sample(secondUsableBatch.batchId(), profile("1.070", "6.0"));
    final var incompleteMeasurement = sample(incompleteBatch.batchId(), acidityOnlyProfile("6.0"));

    when(batchRepository.findAllByCiderMakerId(ciderMakerId))
        .thenReturn(List.of(usableBatch, secondUsableBatch, incompleteBatch));
    when(measurementRepository.findLatestSugarAcidityPointCandidates(any()))
        .thenReturn(List.of(incompleteMeasurement, usableMeasurement, secondUsableMeasurement));

    final var result = service.optimize(command(ciderMakerId, null));

    assertThat(result.sourceCount()).isEqualTo(2);

    final var query = ArgumentCaptor.forClass(LatestFeedstockMeasurementSugarAcidityPointCandidateQuery.class);
    verify(measurementRepository).findLatestSugarAcidityPointCandidates(query.capture());
    assertThat(query.getValue().ciderMakerId()).isEqualTo(ciderMakerId);
    assertThat(query.getValue().limit()).isEqualTo(25);

    final var optimizationCommand = genericCommandCaptor();
    verify(optimizer).optimize(optimizationCommand.capture());
    assertThat(optimizationCommand.getValue().tanninPolicy())
        .isEqualTo(ChemicalProfileCompositionTanninPolicy.CALCULATE_WHEN_COMPLETE);
    assertThat(optimizationCommand.getValue().mode()).isEqualTo(CompositionOptimizationMode.PROPORTION);
    assertThat(optimizationCommand.getValue().algorithm())
        .isEqualTo(CompositionOptimizationAlgorithm.DISCRETIZED_BOUNDED_SEARCH);
    assertThat(optimizationCommand.getValue().calculatorCode())
        .isEqualTo(ChemicalProfileCompositionCalculatorCode.MASS_BALANCE);
    assertThat(optimizationCommand.getValue().normalizerCode()).isEqualTo(ProfileAxisNormalizerCode.EFFECTIVE_RANGE);
    assertThat(optimizationCommand.getValue().sources()).hasSize(2);

    final var source = optimizationCommand.getValue().sources().getFirst();
    assertThat(source.reference().type()).isEqualTo("FEEDSTOCK_BATCH");
    assertThat(source.reference().id()).isEqualTo(usableBatch.batchId().value().toString());
    assertThat(source.availableVolume()).isNotNull();
    assertThat(source.availableVolume().liters()).isEqualByComparingTo("20");
    assertThat(source.axes().valueOf(ChemicalAxis.SUGAR).orElseThrow().value()).isEqualByComparingTo("1.065");
    assertThat(source.axes().valueOf(ChemicalAxis.ACIDITY).orElseThrow().value()).isEqualByComparingTo("5.5");
  }

  @Test
  void requiresTanninWhenTanninTargetIsProvided() {
    final var measurementRepository = mock(FeedstockMeasurementQueryRepository.class);
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var optimizer = mockOptimizer();
    final var service = service(measurementRepository, batchRepository, optimizer);
    final var ciderMakerId = CiderMakerId.of(randomUUID());
    final var firstBatch = batch(ciderMakerId, "20");
    final var secondBatch = batch(ciderMakerId, "30");

    when(batchRepository.findAllByCiderMakerId(ciderMakerId))
        .thenReturn(List.of(firstBatch, secondBatch));
    when(measurementRepository.findLatestSugarAcidityPointCandidates(any()))
        .thenReturn(List.of(
            sample(firstBatch.batchId(), profile("1.065", "5.5")),
            sample(secondBatch.batchId(), profile("1.070", "6.0"))
        ));

    service.optimize(command(ciderMakerId, new TanninTargetRange(
        new BigDecimal("1500"),
        new BigDecimal("2500")
    )));

    final var optimizationCommand = genericCommandCaptor();
    verify(optimizer).optimize(optimizationCommand.capture());
    assertThat(optimizationCommand.getValue().tanninPolicy())
        .isEqualTo(ChemicalProfileCompositionTanninPolicy.REQUIRE_TANNIN);
    assertThat(optimizationCommand.getValue().targetObjective().tanninTarget()).isNotNull();
    assertThat(optimizationCommand.getValue().sources()).hasSize(2);
  }

  @Test
  void rejectsOptimizationWhenLessThanTwoUsableSourcesRemainAfterFiltering() {
    final var measurementRepository = mock(FeedstockMeasurementQueryRepository.class);
    final var batchRepository = mock(FeedstockBatchRepository.class);
    final var optimizer = mockOptimizer();
    final var service = service(measurementRepository, batchRepository, optimizer);
    final var ciderMakerId = CiderMakerId.of(randomUUID());
    final var batch = batch(ciderMakerId, "20");

    when(batchRepository.findAllByCiderMakerId(ciderMakerId))
        .thenReturn(List.of(batch));
    when(measurementRepository.findLatestSugarAcidityPointCandidates(any()))
        .thenReturn(List.of(sample(batch.batchId(), profile("1.065", "5.5"))));

    assertThatThrownBy(() -> service.optimize(command(ciderMakerId, null)))
        .isInstanceOf(InsufficientFeedstockCompositionSourcesException.class)
        .hasMessageContaining("requires at least 2 usable sources");

    verify(optimizer, never()).optimize(any());
  }

  private static OptimizeFeedstockBatchCompositionService service(
      FeedstockMeasurementQueryRepository measurementRepository,
      FeedstockBatchRepository batchRepository,
      OptimizeChemicalProfileCompositionUseCase optimizer
  ) {
    final var thresholds = BuiltInSugarAcidityZoneProfiles.thresholds();
    final var geometryProfile = BuiltInSugarAcidityZoneProfiles.rectangularZoneGeometryProfile();

    return new OptimizeFeedstockBatchCompositionService(
        measurementRepository,
        batchRepository,
        new ChemicalProfileAxisExtractor(),
        new ChemicalProfileCapabilityAssessor(),
        new RectangleSugarAcidityZoneAssessor(thresholds, geometryProfile),
        new ZoneGeometrySugarAcidityTargetResolver(geometryProfile),
        optimizer
    );
  }

  private static OptimizeChemicalProfileCompositionUseCase mockOptimizer() {
    final var optimizer = mock(OptimizeChemicalProfileCompositionUseCase.class);
    when(optimizer.optimize(any()))
        .thenAnswer(invocation -> {
          final org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.OptimizeChemicalProfileCompositionCommand command =
              invocation.getArgument(0);
          return new OptimizeChemicalProfileCompositionResult(
              command.mode(),
              command.algorithm(),
              command.calculatorCode(),
              command.normalizerCode(),
              command.objectiveFunctionCode(),
              new ChemicalProfileCompositionOptimizationResult(0, List.of())
          );
        });
    return optimizer;
  }

  private static ArgumentCaptor<org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.OptimizeChemicalProfileCompositionCommand> genericCommandCaptor() {
    return ArgumentCaptor.forClass(
        org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.OptimizeChemicalProfileCompositionCommand.class
    );
  }

  private static OptimizeFeedstockBatchCompositionCommand command(
      CiderMakerId ciderMakerId,
      TanninTargetRange tanninTarget
  ) {
    return new OptimizeFeedstockBatchCompositionCommand(
        ciderMakerId,
        new FeedstockMeasurementPointFilters(null, null, null, null),
        ProfileTargetScoringPurpose.BLENDING_PLANNING,
        new PredefinedFeedstockBatchBlendProfileTargetSelection(SugarAcidityZone.PERFECT_APPLES),
        tanninTarget,
        ProfileTargetObjectiveWeights.balanced(),
        new DiscretizedCompositionOptimizationSettings(5, 3),
        CompositionOptimizationMode.PROPORTION,
        CompositionOptimizationAlgorithm.DISCRETIZED_BOUNDED_SEARCH,
        ChemicalProfileCompositionCalculatorCode.MASS_BALANCE,
        ProfileAxisNormalizerCode.EFFECTIVE_RANGE,
        ProfileTargetObjectiveFunctionCode.DISTANCE_TO_TARGET_AREA,
        null,
        25
    );
  }

  private static FeedstockBatch batch(CiderMakerId ciderMakerId, String availableVolumeLiters) {
    return FeedstockBatch.create(
        FeedstockBatchId.newId(),
        ciderMakerId,
        FeedstockBatchName.of("Harvest batch"),
        FeedstockType.APPLE_JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-09-01"), LocalDate.parse("2026-12-31")),
        VolumeValue.ofLiters(availableVolumeLiters)
    );
  }

  private static FeedstockMeasurement sample(
      FeedstockBatchId batchId,
      ChemicalProfile profile
  ) {
    return FeedstockMeasurement.create(
        FeedstockMeasurementId.newId(),
        batchId,
        FeedstockMeasurementName.of("Current lab sample"),
        profile,
        Instant.parse("2026-01-15T10:00:00Z")
    );
  }

  private static ChemicalProfile profile(
      String sg,
      String acidity
  ) {
    return ChemicalProfile.builder()
        .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of(sg)
        )))
        .titratableAcidity(ExactMeasurement.of(TitratableAcidityValue.of(acidity)))
        .build();
  }

  private static ChemicalProfile acidityOnlyProfile(String acidity) {
    return ChemicalProfile.builder()
        .titratableAcidity(ExactMeasurement.of(TitratableAcidityValue.of(acidity)))
        .build();
  }
}
