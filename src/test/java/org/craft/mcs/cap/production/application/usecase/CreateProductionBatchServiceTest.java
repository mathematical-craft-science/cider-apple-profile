package org.craft.mcs.cap.production.application.usecase;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.application.usecase.CalculateChemicalProfileCompositionService;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.BuiltInChemicalProfileCompositionCalculatorSelector;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionCalculatorCode;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionTanninPolicy;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.BuiltInProfileAxisNormalizerSelector;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.BuiltInProfileTargetObjectiveFunctionFactory;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.BuiltInSugarAcidityZoneProfiles;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockMeasurementSummary;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockMeasurementQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockMeasurementSugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.core.feedstock.application.port.out.LatestFeedstockMeasurementSugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockBatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.core.production.application.ProductionSourceInsufficientAvailableVolumeException;
import org.craft.mcs.cap.core.production.application.ProductionSourceOwnershipMismatchException;
import org.craft.mcs.cap.core.production.application.port.in.CreateProductionBatchCommand;
import org.craft.mcs.cap.core.production.application.port.in.CreateProductionBatchSourceCommand;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchRepository;
import org.craft.mcs.cap.core.production.application.usecase.CreateProductionBatchService;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchKind;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchName;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static java.util.UUID.randomUUID;
import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class CreateProductionBatchServiceTest {

  @Test
  void createsProductionBatchAndConsumesFeedstockVolumes() {
    final var ciderMakerId = CiderMakerId.of(randomUUID());
    final var firstBatch = batch(ciderMakerId, "50");
    final var secondBatch = batch(ciderMakerId, "60");
    final var batchRepository = new InMemoryFeedstockBatchRepository(firstBatch, secondBatch);
    final var measurementRepository = new InMemoryFeedstockMeasurementQueryRepository(
        measurement(firstBatch.batchId(), profile("1.060", "5.0")),
        measurement(secondBatch.batchId(), profile("1.040", "9.0"))
    );
    final var productionRepository = new CapturingProductionBatchRepository();
    final var service = service(batchRepository, measurementRepository, productionRepository);

    final var result = service.create(command(
        ciderMakerId,
        List.of(
            new CreateProductionBatchSourceCommand(firstBatch.batchId(), VolumeValue.ofLiters("10")),
            new CreateProductionBatchSourceCommand(secondBatch.batchId(), VolumeValue.ofLiters("30"))
        )
    ));

    assertThat(result.productionBatch().kind()).isEqualTo(ProductionBatchKind.CIDER);
    assertThat(result.productionBatch().sourceComposition().totalVolume().liters()).isEqualByComparingTo("40");
    assertThat(result.composition().composition().profile().sugar().value()).isEqualByComparingTo("1.045000000");
    assertThat(result.composition().composition().profile().acidity().value()).isEqualByComparingTo("8.000000000");
    assertThat(batchRepository.findById(firstBatch.batchId()).orElseThrow().availableVolume().liters())
        .isEqualByComparingTo("40");
    assertThat(batchRepository.findById(secondBatch.batchId()).orElseThrow().availableVolume().liters())
        .isEqualByComparingTo("30");
    assertThat(productionRepository.saved).isNotNull();
  }

  @Test
  void rejectsSourceOwnedByAnotherCiderMaker() {
    final var ciderMakerId = CiderMakerId.of(randomUUID());
    final var otherBatch = batch(CiderMakerId.of(randomUUID()), "50");
    final var batchRepository = new InMemoryFeedstockBatchRepository(otherBatch);
    final var service = service(
        batchRepository,
        new InMemoryFeedstockMeasurementQueryRepository(measurement(otherBatch.batchId(), profile("1.060", "5.0"))),
        new CapturingProductionBatchRepository()
    );

    assertThatThrownBy(() -> service.create(command(
        ciderMakerId,
        List.of(new CreateProductionBatchSourceCommand(otherBatch.batchId(), VolumeValue.ofLiters("10")))
    )))
        .isInstanceOf(ProductionSourceOwnershipMismatchException.class);

    assertThat(batchRepository.findById(otherBatch.batchId()).orElseThrow().availableVolume().liters())
        .isEqualByComparingTo("50");
  }

  @Test
  void rejectsSourceVolumeGreaterThanAvailableVolume() {
    final var ciderMakerId = CiderMakerId.of(randomUUID());
    final var batch = batch(ciderMakerId, "5");
    final var service = service(
        new InMemoryFeedstockBatchRepository(batch),
        new InMemoryFeedstockMeasurementQueryRepository(measurement(batch.batchId(), profile("1.060", "5.0"))),
        new CapturingProductionBatchRepository()
    );

    assertThatThrownBy(() -> service.create(command(
        ciderMakerId,
        List.of(new CreateProductionBatchSourceCommand(batch.batchId(), VolumeValue.ofLiters("10")))
    )))
        .isInstanceOf(ProductionSourceInsufficientAvailableVolumeException.class);
  }

  private static @NonNull CreateProductionBatchService service(
      @NonNull FeedstockBatchRepository batchRepository,
      @NonNull FeedstockMeasurementQueryRepository measurementRepository,
      @NonNull ProductionBatchRepository productionRepository
  ) {
    return new CreateProductionBatchService(
        batchRepository,
        measurementRepository,
        productionRepository,
        new ChemicalProfileAxisExtractor(),
        new ChemicalProfileCapabilityAssessor(),
        new CalculateChemicalProfileCompositionService(
            new BuiltInChemicalProfileCompositionCalculatorSelector(),
            new BuiltInProfileAxisNormalizerSelector(BuiltInSugarAcidityZoneProfiles.thresholds()),
            new BuiltInProfileTargetObjectiveFunctionFactory()
        )
    );
  }

  private static @NonNull CreateProductionBatchCommand command(
      @NonNull CiderMakerId ciderMakerId,
      @NonNull List<@NonNull CreateProductionBatchSourceCommand> sources
  ) {
    return new CreateProductionBatchCommand(
        ciderMakerId,
        ProductionBatchName.of("Tank A"),
        ProductionBatchKind.CIDER,
        sources,
        ChemicalProfileCompositionTanninPolicy.CALCULATE_WHEN_COMPLETE,
        null,
        ProfileTargetObjectiveWeights.balanced(),
        ProfileTargetScoringPurpose.BLENDING_PLANNING,
        ChemicalProfileCompositionCalculatorCode.MASS_BALANCE,
        ProfileAxisNormalizerCode.EFFECTIVE_RANGE,
        ProfileTargetObjectiveFunctionCode.DISTANCE_TO_TARGET_AREA,
        null
    );
  }

  private static @NonNull FeedstockBatch batch(
      @NonNull CiderMakerId ciderMakerId,
      @NonNull String availableVolumeLiters
  ) {
    return FeedstockBatch.create(
        FeedstockBatchId.newId(),
        ciderMakerId,
        FeedstockBatchName.of("Harvest batch"),
        FeedstockType.APPLE_JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-09-01"), LocalDate.parse("2026-12-31")),
        VolumeValue.ofLiters(availableVolumeLiters)
    );
  }

  private static @NonNull FeedstockMeasurement measurement(
      @NonNull FeedstockBatchId batchId,
      @NonNull ChemicalProfile profile
  ) {
    return FeedstockMeasurement.create(
        FeedstockMeasurementId.newId(),
        batchId,
        FeedstockMeasurementName.of("Current lab sample"),
        profile,
        feedstockMeasurementInstant()
    );
  }

  private static @NonNull ChemicalProfile profile(
      @NonNull String sg,
      @NonNull String acidity
  ) {
    return ChemicalProfile.builder()
        .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(SpecificGravityValue.of(sg))))
        .titratableAcidity(ExactMeasurement.of(TitratableAcidityValue.of(acidity)))
        .build();
  }

  private static final class InMemoryFeedstockBatchRepository
      implements FeedstockBatchRepository {

    private final @NonNull HashMap<@NonNull FeedstockBatchId, @NonNull FeedstockBatch> batches = new HashMap<>();

    InMemoryFeedstockBatchRepository(@NonNull FeedstockBatch... batches) {
      for (final var batch : batches) {
        this.batches.put(batch.batchId(), batch);
      }
    }

    @Override
    public @NonNull FeedstockBatch save(@NonNull FeedstockBatch feedstockBatch) {
      batches.put(feedstockBatch.batchId(), feedstockBatch);
      return feedstockBatch;
    }

    @Override
    public @NonNull Optional<@NonNull FeedstockBatch> findById(@NonNull FeedstockBatchId batchId) {
      return Optional.ofNullable(batches.get(batchId));
    }

    @Override
    public @NonNull List<@NonNull FeedstockBatch> findAllByCiderMakerId(@NonNull CiderMakerId ciderMakerId) {
      return batches.values().stream()
          .filter(batch -> batch.ciderMakerId().equals(ciderMakerId))
          .toList();
    }

    @Override
    public void delete(@NonNull FeedstockBatch feedstockBatch) {
      batches.remove(feedstockBatch.batchId());
    }
  }

  private static final class InMemoryFeedstockMeasurementQueryRepository
      implements FeedstockMeasurementQueryRepository {

    private final @NonNull HashMap<@NonNull FeedstockBatchId, @NonNull FeedstockMeasurement> measurements =
        new HashMap<>();

    InMemoryFeedstockMeasurementQueryRepository(@NonNull FeedstockMeasurement... measurements) {
      for (final var measurement : measurements) {
        this.measurements.put(measurement.batchId(), measurement);
      }
    }

    @Override
    public @NonNull List<@NonNull FeedstockMeasurement> findAllByCiderMakerId(@NonNull CiderMakerId ciderMakerId) {
      return List.of();
    }

    @Override
    public @NonNull List<@NonNull FeedstockMeasurement> findAllByCiderMakerIdAndSeason(
        @NonNull CiderMakerId ciderMakerId,
        @NonNull FeedstockSeason season
    ) {
      return List.of();
    }

    @Override
    public @NonNull List<@NonNull FeedstockMeasurement> findSugarAcidityPointCandidates(
        @NonNull FeedstockMeasurementSugarAcidityPointCandidateQuery query
    ) {
      return List.of();
    }

    @Override
    public @NonNull List<@NonNull FeedstockMeasurement> findLatestSugarAcidityPointCandidates(
        @NonNull LatestFeedstockMeasurementSugarAcidityPointCandidateQuery query
    ) {
      return List.of();
    }

    @Override
    public @NonNull Optional<@NonNull FeedstockMeasurement> findLatestByBatchId(@NonNull FeedstockBatchId batchId) {
      return Optional.ofNullable(measurements.get(batchId));
    }

    @Override
    public @NonNull List<@NonNull FeedstockMeasurementSummary> findSummariesByBatchId(
        @NonNull FeedstockBatchId batchId
    ) {
      return List.of();
    }
  }

  private static final class CapturingProductionBatchRepository
      implements ProductionBatchRepository {

    private ProductionBatch saved;

    @Override
    public @NonNull ProductionBatch save(@NonNull ProductionBatch productionBatch) {
      saved = productionBatch;
      return productionBatch;
    }
  }
}
