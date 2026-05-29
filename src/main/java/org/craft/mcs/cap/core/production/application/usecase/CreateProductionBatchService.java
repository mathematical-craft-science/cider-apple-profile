package org.craft.mcs.cap.core.production.application.usecase;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.production.application.ProductionSourceInsufficientAvailableVolumeException;
import org.craft.mcs.cap.core.production.application.ProductionSourceOwnershipMismatchException;
import org.craft.mcs.cap.core.production.application.port.in.CreateProductionBatchCommand;
import org.craft.mcs.cap.core.production.application.port.in.CreateProductionBatchUseCase;
import org.craft.mcs.cap.core.production.application.port.in.ProductionBatchCreated;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchRepository;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchFeedstockTraceSource;
import org.craft.mcs.cap.core.production.application.port.out.ProductionFeedstockTraceSnapshotProvider;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchTraceRecorder;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchTraceSource;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.domain.model.ProductionInput;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Creates a production batch from an already accepted production input.
 *
 * <p>Composition calculation, optimization and target scoring happen before this use case. This
 * service only validates source ownership/stock, consumes feedstock volume atomically and persists
 * the committed production batch. It then records the production-created traceability fact through
 * a production-owned outbound port, keeping traceability mapping outside this use case.
 */
@Component
@RequiredArgsConstructor
public class CreateProductionBatchService
    implements CreateProductionBatchUseCase {

  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull ProductionBatchRepository productionBatchRepository;
  private final @NonNull ProductionBatchTraceRecorder traceRecorder;
  private final @NonNull ProductionFeedstockTraceSnapshotProvider feedstockTraceSnapshotProvider;
  private final @NonNull TransactionHelper transactionHelper;
  private final @NonNull Clock clock;

  @Override
  public @NonNull ProductionBatchCreated create(@NonNull CreateProductionBatchCommand command) {
    requireNonNull(command, "create production batch command cannot be null");

    return transactionHelper.callInTransactionNonNull(() -> {
      final var batchesById = loadAndValidateBatches(command);
      final var input = command.input();
      final var productionBatch = ProductionBatch.create(
          ProductionBatchId.newId(),
          command.ciderMakerId(),
          command.name(),
          command.kind(),
          input.sourceComposition().totalVolume(),
          input.profile(),
          input.targetDistance(),
          command.note()
      );

      consumeFeedstockVolumes(command, batchesById);
      final var savedProductionBatch = productionBatchRepository.save(productionBatch);
      traceRecorder.recordCreated(
          savedProductionBatch,
          traceSources(input, batchesById, feedstockTraceSnapshotProvider),
          Instant.now(clock),
          command.note()
      );

      return new ProductionBatchCreated(savedProductionBatch, input);
    });
  }

  private @NonNull Map<@NonNull FeedstockBatchId, @NonNull FeedstockBatch> loadAndValidateBatches(
      @NonNull CreateProductionBatchCommand command
  ) {
    final var sources = command.input().sourceComposition().sources();
    final Map<@NonNull FeedstockBatchId, @NonNull FeedstockBatch> batchesById =
        LinkedHashMap.newLinkedHashMap(sources.size());

    for (final var source : sources) {
      final var batch = feedstockBatchRepository.findById(source.feedstockBatchId())
          .orElseThrow(() -> new FeedstockBatchNotFoundException(source.feedstockBatchId()));
      if (!batch.ciderMakerId().equals(command.ciderMakerId())) {
        throw new ProductionSourceOwnershipMismatchException(
            batch.batchId(),
            command.ciderMakerId(),
            batch.ciderMakerId()
        );
      }
      if (source.volume().exceeds(batch.availableVolume())) {
        throw new ProductionSourceInsufficientAvailableVolumeException(
            batch.batchId(),
            source.volume(),
            batch.availableVolume()
        );
      }
      batchesById.put(source.feedstockBatchId(), batch);
    }

    return Map.copyOf(batchesById);
  }

  private void consumeFeedstockVolumes(
      @NonNull CreateProductionBatchCommand command,
      @NonNull Map<@NonNull FeedstockBatchId, @NonNull FeedstockBatch> batchesById
  ) {
    final var sources = command.input().sourceComposition().sources();

    for (final ProductionSourceComponent source : sources) {
      final var batch = batchFor(source.feedstockBatchId(), batchesById);
      batch.consumeAvailableVolume(source.volume());
      feedstockBatchRepository.save(batch);
    }
  }

  private static @NonNull List<@NonNull ProductionBatchTraceSource> traceSources(
      @NonNull ProductionInput input,
      @NonNull Map<@NonNull FeedstockBatchId, @NonNull FeedstockBatch> batchesById,
      @NonNull ProductionFeedstockTraceSnapshotProvider feedstockTraceSnapshotProvider
  ) {
    final var sources = input.sourceComposition().sources();
    final var traceSources = new ArrayList<ProductionBatchTraceSource>(sources.size());

    for (final var source : sources) {
      final var batch = batchFor(source.feedstockBatchId(), batchesById);
      final var snapshot = feedstockTraceSnapshotProvider.currentSnapshotFor(batch.batchId());
      traceSources.add(new ProductionBatchFeedstockTraceSource(
          batch.batchId(),
          batch.name().value(),
          source.volume(),
          snapshot.profile(),
          snapshot.confidence()
      ));
    }

    return List.copyOf(traceSources);
  }

  private static @NonNull FeedstockBatch batchFor(
      @NonNull FeedstockBatchId batchId,
      @NonNull Map<@NonNull FeedstockBatchId, @NonNull FeedstockBatch> batchesById
  ) {
    return Optional.ofNullable(batchesById.get(batchId))
        .orElseThrow(() -> new IllegalStateException("loaded feedstock batch map does not contain source " + batchId.value()));
  }
}
