package org.craft.mcs.cap.feedstock.application.planning.composition.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.core.feedstock.application.batch.usecase.FeedstockBatchSummaryMapper.toSummary;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures
    .graphEvaluator;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.craft.mcs.cap.shared.testing.FeedstockCurrentProfileViews.currentProfile;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementInstant;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementRecordedAt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.model.FeedstockAnalysisCandidateFilters;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.port.out.CurrentFeedstockBatchAnalysisCandidateQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.port.out.FeedstockCurrentProfileAnalysisCandidateRepository;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.service.FeedstockGraphCandidateMatcher;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.PredefinedFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchSummaryQueryRepository;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetName;
import org.craft.mcs.cap.core.feedstock.application.planning.composition.InsufficientFeedstockCompositionSourcesException;
import org.craft.mcs.cap.core.feedstock.application.planning.composition.port.in.OptimizeFeedstockBatchCompositionCommand;
import org.craft.mcs.cap.core.feedstock.application.planning.composition.usecase.OptimizeFeedstockBatchCompositionService;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionCalculatorCode;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionTanninPolicy;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.port.in.OptimizeCompositionCommand;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.port.in.OptimizeCompositionResult;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.port.in.OptimizeCompositionUseCase;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.CompositionOptimizationMode;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.DiscretizedCompositionOptimizationSettings;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.BuiltInJolicoeurSugarAcidityZoneProfiles;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZone;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringConfiguration;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.TanninTargetRange;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.ZoneGeometrySugarAcidityTargetResolver;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.testing.ProfileCharacteristicsFixture;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.shared.kernel.material.value.Vessel;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.testing.FeedstockMeasurementFixture;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;


class OptimizeFeedstockBatchCompositionServiceTest {

  @Test
  void buildsOptimizationProblemFromCurrentFeedstockBatchMeasurements() {
    final var measurementRepository = mock(FeedstockCurrentProfileAnalysisCandidateRepository.class);
    final var batchRepository = mock(FeedstockBatchSummaryQueryRepository.class);
    final var optimizer = mockOptimizer();
    final var service = service(measurementRepository, batchRepository, optimizer);
    final var workspaceId = WorkspaceId.of(newUuid());
    final var usableBatch = batch(workspaceId, "20");
    final var secondUsableBatch = batch(workspaceId, "30");
    final var incompleteBatch = batch(workspaceId, "10");
    final var usableMeasurement = sample(usableBatch.batchId(), profile("1.065", "5.5"));
    final var secondUsableMeasurement = sample(secondUsableBatch.batchId(), profile("1.070", "6.0"));
    final var incompleteMeasurement = sample(incompleteBatch.batchId(), acidityOnlyProfile("6.0"));

    when(batchRepository.findAllByWorkspaceId(workspaceId))
        .thenReturn(List.of(
            toSummary(usableBatch),
            toSummary(secondUsableBatch),
            toSummary(incompleteBatch)
        ));
    when(measurementRepository.findCandidates(any()))
        .thenReturn(List.of(
            currentProfile(incompleteMeasurement),
            currentProfile(usableMeasurement),
            currentProfile(secondUsableMeasurement)
        ));

    final var result = service.optimize(command(workspaceId, null));

    assertThat(result.sourceCount()).isEqualTo(2);

    final var query = ArgumentCaptor.forClass(CurrentFeedstockBatchAnalysisCandidateQuery.class);
    verify(measurementRepository).findCandidates(query.capture());
    assertThat(query.getValue().workspaceId()).isEqualTo(workspaceId);
    assertThat(query.getValue().limit()).isEqualTo(25);

    final var optimizationCommand = genericCommandCaptor();
    verify(optimizer).optimize(optimizationCommand.capture());
    assertThat(optimizationCommand.getValue().tanninPolicy())
        .isEqualTo(CompositionTanninPolicy.CALCULATE_WHEN_COMPLETE);
    assertThat(optimizationCommand.getValue().mode()).isEqualTo(CompositionOptimizationMode.PROPORTION);
    assertThat(optimizationCommand.getValue().algorithm())
        .isEqualTo(CompositionOptimizationAlgorithm.DISCRETIZED_BOUNDED_SEARCH);
    assertThat(optimizationCommand.getValue().calculatorCode())
        .isEqualTo(CompositionCalculatorCode.MASS_BALANCE);
    assertThat(optimizationCommand.getValue().scoringConfiguration().normalizerCode())
        .isEqualTo(ProfileAxisNormalizerCode.EFFECTIVE_RANGE);
    assertThat(optimizationCommand.getValue().sources()).hasSize(2);

    final var source = optimizationCommand.getValue().sources().getFirst();
    assertThat(source.reference().type()).isEqualTo("FEEDSTOCK_BATCH");
    assertThat(source.reference().id()).isEqualTo(usableBatch.batchId().value().toString());
    assertThat(source.availableVolume()).isNotNull();
    assertThat(source.availableVolume().liters()).isEqualByComparingTo("20");
    assertThat(point(source.characteristics().specificGravity()).asBigDecimal())
        .isEqualByComparingTo("1.065");
    assertThat(point(source.characteristics().titratableAcidity()).asBigDecimal())
        .isEqualByComparingTo("5.5");
  }

  private static <T extends Comparable<? super T>> @NonNull T point(
      @NonNull CharacteristicValue<T> value
  ) {
    return switch (value) {
      case PointCharacteristicValue<T>(var point) -> point;
      case IntervalCharacteristicValue<T> _ ->
          throw new AssertionError("expected point characteristic");
    };
  }

  @Test
  void requiresTanninWhenTanninTargetIsProvided() {
    final var measurementRepository = mock(FeedstockCurrentProfileAnalysisCandidateRepository.class);
    final var batchRepository = mock(FeedstockBatchSummaryQueryRepository.class);
    final var optimizer = mockOptimizer();
    final var service = service(measurementRepository, batchRepository, optimizer);
    final var workspaceId = WorkspaceId.of(newUuid());
    final var firstBatch = batch(workspaceId, "20");
    final var secondBatch = batch(workspaceId, "30");

    when(batchRepository.findAllByWorkspaceId(workspaceId))
        .thenReturn(List.of(toSummary(firstBatch), toSummary(secondBatch)));
    when(measurementRepository.findCandidates(any()))
        .thenReturn(List.of(
            currentProfile(sample(firstBatch.batchId(), profile("1.065", "5.5"))),
            currentProfile(sample(secondBatch.batchId(), profile("1.070", "6.0")))
        ));

    service.optimize(command(workspaceId, new TanninTargetRange(
        TanninValue.of("1500"),
        TanninValue.of("2500")
    )));

    final var optimizationCommand = genericCommandCaptor();
    verify(optimizer).optimize(optimizationCommand.capture());
    assertThat(optimizationCommand.getValue().tanninPolicy())
        .isEqualTo(CompositionTanninPolicy.REQUIRE_TANNIN);
    assertThat(optimizationCommand.getValue().targetObjective().tanninTarget()).isNotNull();
    assertThat(optimizationCommand.getValue().sources()).hasSize(2);
  }

  @Test
  void rejectsOptimizationWhenLessThanTwoUsableSourcesRemainAfterFiltering() {
    final var measurementRepository = mock(FeedstockCurrentProfileAnalysisCandidateRepository.class);
    final var batchRepository = mock(FeedstockBatchSummaryQueryRepository.class);
    final var optimizer = mockOptimizer();
    final var service = service(measurementRepository, batchRepository, optimizer);
    final var workspaceId = WorkspaceId.of(newUuid());
    final var batch = batch(workspaceId, "20");

    when(batchRepository.findAllByWorkspaceId(workspaceId))
        .thenReturn(List.of(toSummary(batch)));
    when(measurementRepository.findCandidates(any()))
        .thenReturn(List.of(currentProfile(sample(batch.batchId(), profile("1.065", "5.5")))));

    assertThatThrownBy(() -> service.optimize(command(workspaceId, null)))
        .isInstanceOf(InsufficientFeedstockCompositionSourcesException.class)
        .hasMessageContaining("requires at least 2 usable sources");

    verify(optimizer, never()).optimize(any());
  }

  private static OptimizeFeedstockBatchCompositionService service(
      FeedstockCurrentProfileAnalysisCandidateRepository measurementRepository,
      FeedstockBatchSummaryQueryRepository batchRepository,
      OptimizeCompositionUseCase optimizer
  ) {
    final var geometryProfile = BuiltInJolicoeurSugarAcidityZoneProfiles.rectangularZoneGeometryProfile();

    return new OptimizeFeedstockBatchCompositionService(
        measurementRepository,
        batchRepository,
        graphEvaluator(geometryProfile),
        new FeedstockGraphCandidateMatcher(),
        new ZoneGeometrySugarAcidityTargetResolver(geometryProfile),
        optimizer,
        mock(FeedstockAccessControl.class)
    );
  }

  private static OptimizeCompositionUseCase mockOptimizer() {
    final var optimizer = mock(OptimizeCompositionUseCase.class);
    when(optimizer.optimize(any()))
        .thenAnswer(invocation -> {
          final OptimizeCompositionCommand command = invocation.getArgument(0);
          return new OptimizeCompositionResult(
              command.mode(),
              command.algorithm(),
              command.calculatorCode(),
              command.scoringConfiguration().normalizerCode(),
              command.scoringConfiguration().objectiveFunctionCode(),
              command.settings(),
              0,
              List.of()
          );
        });
    return optimizer;
  }

  private static ArgumentCaptor<OptimizeCompositionCommand> genericCommandCaptor() {
    return ArgumentCaptor.forClass(OptimizeCompositionCommand.class);
  }

  private static OptimizeFeedstockBatchCompositionCommand command(
      WorkspaceId workspaceId,
      TanninTargetRange tanninTarget
  ) {
    return new OptimizeFeedstockBatchCompositionCommand(
        workspaceId,
        new FeedstockAnalysisCandidateFilters(null, null, null, null),
        new PredefinedFeedstockProfileTargetSelection(JolicoeurSugarAcidityZone.PERFECT_APPLES),
        tanninTarget,
        new ProfileTargetScoringConfiguration(
            ProfileAxisNormalizerCode.EFFECTIVE_RANGE,
            ProfileTargetObjectiveFunctionCode.DISTANCE_TO_TARGET_AREA,
            ProfileTargetObjectiveWeights.balanced()
        ),
        List.of(),
        new DiscretizedCompositionOptimizationSettings(5, 3),
        CompositionOptimizationMode.PROPORTION,
        CompositionOptimizationAlgorithm.DISCRETIZED_BOUNDED_SEARCH,
        CompositionCalculatorCode.MASS_BALANCE,
        null,
        25
    );
  }

  private static FeedstockBatch batch(
      WorkspaceId workspaceId,
      String availableVolumeLiters
  ) {
    return FeedstockBatch.create(
        FeedstockBatchId.newId(),
        workspaceId,
        BatchName.of("Harvest batch"),
        Vessel.named("Tank A-01"),
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-09-01"), LocalDate.parse("2026-12-31")),
        VolumeValue.ofLiters(availableVolumeLiters)
    );
  }

  private static FeedstockMeasurementFixture sample(
      FeedstockBatchId batchId,
      ProfileCharacteristicsFixture profile
  ) {
    return FeedstockMeasurementFixture.create(
        MeasurementSetId.newId(),
        batchId,
        MeasurementSetName.of("Current lab sample"),
        profile,
        feedstockMeasurementInstant(),
        feedstockMeasurementRecordedAt()
    );
  }

  private static ProfileCharacteristicsFixture profile(
      String sg,
      String acidity
  ) {
    return ProfileCharacteristicsFixture.builder()
        .specificGravity(PointCharacteristicValue.of(SpecificGravityValue.of(sg)))
        .titratableAcidity(PointCharacteristicValue.of(TitratableAcidityValue.of(acidity)))
        .build();
  }

  private static ProfileCharacteristicsFixture acidityOnlyProfile(String acidity) {
    return ProfileCharacteristicsFixture.builder()
        .titratableAcidity(PointCharacteristicValue.of(TitratableAcidityValue.of(acidity)))
        .build();
  }
}
