package org.craft.mcs.cap.infrastructure.adapter.out.production;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.measurement.query.FeedstockBatchCurrentMeasurementQueryService;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.production.application.ProductionSourceCurrentProfileUnavailableException;
import org.craft.mcs.cap.core.production.application.ProductionSourceInsufficientAvailableVolumeException;
import org.craft.mcs.cap.core.production.application.ProductionSourceNotFoundException;
import org.craft.mcs.cap.core.production.application.ProductionSourceOwnershipMismatchException;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchFeedstockTraceSource;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchTraceSource;
import org.craft.mcs.cap.core.production.application.port.out.ProductionSourceConsumptionPort;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Anti-corruption adapter between production source consumption and feedstock batches.
 *
 * <p>Production works with {@link ProductionSourceReference}; this adapter interprets
 * {@code FEEDSTOCK_BATCH}, enforces feedstock ownership/available-volume rules, consumes material
 * volume and builds a trace snapshot from the current feedstock measurement.
 */
@Component
@RequiredArgsConstructor
class FeedstockProductionSourceConsumptionAdapter implements ProductionSourceConsumptionPort {

  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull FeedstockBatchCurrentMeasurementQueryService currentMeasurementQueryService;
  private final @NonNull ChemicalProfileAxisExtractor axisExtractor;

  @Override
  public @NonNull List<@NonNull ProductionBatchTraceSource> consumeAndSnapshotSources(
      @NonNull CiderMakerId ciderMakerId,
      @NonNull List<@NonNull ProductionSourceComponent> sources
  ) {
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
    sources = List.copyOf(requireNonNull(sources, "production sources cannot be null"));

    final var traceSources = new ArrayList<ProductionBatchTraceSource>(sources.size());
    for (final var source : sources) {
      traceSources.add(consumeAndSnapshotSource(ciderMakerId, source));
    }

    return List.copyOf(traceSources);
  }

  private @NonNull ProductionBatchTraceSource consumeAndSnapshotSource(
      @NonNull CiderMakerId ciderMakerId,
      @NonNull ProductionSourceComponent source
  ) {
    return switch (source.reference().type()) {
      case FEEDSTOCK_BATCH -> consumeAndSnapshotFeedstockBatch(ciderMakerId, source);
    };
  }

  private @NonNull ProductionBatchTraceSource consumeAndSnapshotFeedstockBatch(
      @NonNull CiderMakerId ciderMakerId,
      @NonNull ProductionSourceComponent source
  ) {
    final var batchId = FeedstockBatchId.of(source.reference().id());
    final var batch = feedstockBatchRepository.findById(batchId)
        .orElseThrow(() -> new ProductionSourceNotFoundException(source.reference()));

    validateOwnership(ciderMakerId, source.reference(), batch);
    validateAvailableVolume(source, batch);

    final var traceSource = traceSource(source, batch);
    batch.consumeAvailableVolume(source.volume());
    feedstockBatchRepository.save(batch);
    return traceSource;
  }

  private static void validateOwnership(
      @NonNull CiderMakerId ciderMakerId,
      @NonNull ProductionSourceReference sourceReference,
      @NonNull FeedstockBatch batch
  ) {
    if (!batch.ciderMakerId().equals(ciderMakerId)) {
      throw new ProductionSourceOwnershipMismatchException(
          sourceReference,
          ciderMakerId,
          batch.ciderMakerId()
      );
    }
  }

  private static void validateAvailableVolume(
      @NonNull ProductionSourceComponent source,
      @NonNull FeedstockBatch batch
  ) {
    if (source.volume().exceeds(batch.availableVolume())) {
      throw new ProductionSourceInsufficientAvailableVolumeException(
          source.reference(),
          source.volume(),
          batch.availableVolume()
      );
    }
  }

  private @NonNull ProductionBatchTraceSource traceSource(
      @NonNull ProductionSourceComponent source,
      @NonNull FeedstockBatch batch
  ) {
    final var measurement = currentMeasurementQueryService.findCurrentMeasurement(batch.batchId())
        .orElseThrow(() -> new ProductionSourceCurrentProfileUnavailableException(source.reference()));
    final var profile = measurement.chemicalProfile();

    return new ProductionBatchFeedstockTraceSource(
        batch.batchId(),
        batch.name().value(),
        source.volume(),
        axisExtractor.extract(profile),
        profile.confidence()
    );
  }
}
