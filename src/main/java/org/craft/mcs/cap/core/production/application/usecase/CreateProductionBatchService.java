package org.craft.mcs.cap.core.production.application.usecase;

import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.application.port.in.CalculateChemicalProfileCompositionCommand;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.application.port.in.CalculateChemicalProfileCompositionResult;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.application.port.in.CalculateChemicalProfileCompositionUseCase;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.AbsoluteCompositionVolume;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionSource;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionSourceReference;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchHasNoMeasurementsException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockMeasurementQueryRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.production.application.ProductionSourceInsufficientAvailableVolumeException;
import org.craft.mcs.cap.core.production.application.ProductionSourceOwnershipMismatchException;
import org.craft.mcs.cap.core.production.application.ProductionSourceProfileNotUsableException;
import org.craft.mcs.cap.core.production.application.port.in.CreateProductionBatchCommand;
import org.craft.mcs.cap.core.production.application.port.in.CreateProductionBatchSourceCommand;
import org.craft.mcs.cap.core.production.application.port.in.CreateProductionBatchUseCase;
import org.craft.mcs.cap.core.production.application.port.in.ProductionBatchCreated;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchRepository;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComposition;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapability;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.requireNonNull;


/**
 * Creates a production batch from concrete feedstock batch volumes.
 *
 * <p>This application service is intentionally orchestration-heavy: production owns the committed
 * source composition, feedstock owns stock availability, and cider-analysis owns chemical profile calculation. The
 * transaction keeps stock consumption and production-batch persistence atomic.
 */
@Component
@RequiredArgsConstructor
public class CreateProductionBatchService
    implements CreateProductionBatchUseCase {

  private static final @NonNull String SOURCE_TYPE_FEEDSTOCK_BATCH = "FEEDSTOCK_BATCH";

  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull FeedstockMeasurementQueryRepository feedstockMeasurementQueryRepository;
  private final @NonNull ProductionBatchRepository productionBatchRepository;
  private final @NonNull ChemicalProfileAxisExtractor axisExtractor;
  private final @NonNull ChemicalProfileCapabilityAssessor capabilityAssessor;
  private final @NonNull CalculateChemicalProfileCompositionUseCase calculateChemicalProfileCompositionUseCase;

  private static @NonNull ProductionSourceComposition sourceComposition(
      @NonNull CreateProductionBatchCommand command
  ) {
    return ProductionSourceComposition.of(command.sources().stream()
        .map(CreateProductionBatchService::sourceComponent)
        .toList());
  }

  private static @NonNull ProductionSourceComponent sourceComponent(
      @NonNull CreateProductionBatchSourceCommand source
  ) {
    return new ProductionSourceComponent(source.feedstockBatchId(), source.volume());
  }

  private static @NonNull ProductionProfileSnapshot profileSnapshot(
      @NonNull CalculateChemicalProfileCompositionResult result
  ) {
    final var point = result.composition().profile();
    return new ProductionProfileSnapshot(
        SpecificGravityValue.of(point.sugar().value()),
        TitratableAcidityValue.of(point.acidity().value()),
        point.tannin() == null ? null : TanninityValue.of(point.tannin().value()) // NOSONAR
    );
  }

  @Override
  @Transactional
  public @NonNull ProductionBatchCreated create(@NonNull CreateProductionBatchCommand command) {
    requireNonNull(command, "create production batch command cannot be null");

    final var batches = loadAndValidateBatches(command);
    final var composition = calculateComposition(command, batches);
    final var productionBatch = ProductionBatch.create(
        ProductionBatchId.newId(),
        command.ciderMakerId(),
        command.name(),
        command.kind(),
        sourceComposition(command),
        profileSnapshot(composition),
        composition.targetDistance() == null ? null : composition.targetDistance().value(), // NOSONAR
        command.note()
    );

    consumeFeedstockVolumes(command, batches);
    final var savedProductionBatch = productionBatchRepository.save(productionBatch);

    return new ProductionBatchCreated(savedProductionBatch, composition);
  }

  private @NonNull ArrayList<@NonNull FeedstockBatch> loadAndValidateBatches(
      @NonNull CreateProductionBatchCommand command
  ) {
    final var batches = new ArrayList<FeedstockBatch>(command.sources().size());

    for (final var source : command.sources()) {
      final var batch = feedstockBatchRepository.findById(source.feedstockBatchId())
          .orElseThrow(() -> new FeedstockBatchNotFoundException(source.feedstockBatchId()));
      if (!batch.ciderMakerId().equals(command.ciderMakerId())) {
        throw new ProductionSourceOwnershipMismatchException(
            batch.batchId(),
            command.ciderMakerId(),
            batch.ciderMakerId()
        );
      }
      if (source.volume().compareTo(batch.availableVolume()) > 0) {
        throw new ProductionSourceInsufficientAvailableVolumeException(
            batch.batchId(),
            source.volume(),
            batch.availableVolume()
        );
      }
      batches.add(batch);
    }

    return batches;
  }

  private @NonNull CalculateChemicalProfileCompositionResult calculateComposition(
      @NonNull CreateProductionBatchCommand command,
      @NonNull ArrayList<@NonNull FeedstockBatch> batches
  ) {
    final var sources = new ArrayList<ChemicalProfileCompositionSource>(command.sources().size());

    for (int i = 0; i < command.sources().size(); i++) {
      final var source = command.sources().get(i);
      final var batch = batches.get(i);
      final var axes = currentAxes(batch);

      sources.add(new ChemicalProfileCompositionSource(
          new ChemicalProfileCompositionSourceReference(
              SOURCE_TYPE_FEEDSTOCK_BATCH,
              source.feedstockBatchId().value().toString()
          ),
          axes,
          new AbsoluteCompositionVolume(source.volume()),
          batch.availableVolume()
      ));
    }

    return calculateChemicalProfileCompositionUseCase.calculate(new CalculateChemicalProfileCompositionCommand(
        sources,
        command.tanninPolicy(),
        command.targetObjective(),
        command.targetWeights(),
        command.scoringPurpose(),
        command.calculatorCode(),
        command.normalizerCode(),
        command.objectiveFunctionCode()
    ));
  }

  private @NonNull ChemicalProfileAxes currentAxes(@NonNull FeedstockBatch batch) {
    final var measurement = feedstockMeasurementQueryRepository.findLatestByBatchId(batch.batchId())
        .orElseThrow(() -> new FeedstockBatchHasNoMeasurementsException(batch.batchId()));
    final var axes = axisExtractor.extract(measurement.chemicalProfile());

    if (!capabilityAssessor.assess(axes, ChemicalProfileCapability.SUGAR_ACIDITY_PLOT).capable()) {
      throw new ProductionSourceProfileNotUsableException(
          batch.batchId(),
          ChemicalProfileCapability.SUGAR_ACIDITY_PLOT
      );
    }

    return axes;
  }

  private void consumeFeedstockVolumes(
      @NonNull CreateProductionBatchCommand command,
      @NonNull ArrayList<@NonNull FeedstockBatch> batches
  ) {
    for (int i = 0; i < command.sources().size(); i++) {
      final var batch = batches.get(i);
      batch.consumeAvailableVolume(command.sources().get(i).volume());
      feedstockBatchRepository.save(batch);
    }
  }
}
