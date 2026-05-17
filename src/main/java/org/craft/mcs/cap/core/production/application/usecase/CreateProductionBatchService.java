package org.craft.mcs.cap.core.production.application.usecase;

import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.production.application.ProductionSourceInsufficientAvailableVolumeException;
import org.craft.mcs.cap.core.production.application.ProductionSourceOwnershipMismatchException;
import org.craft.mcs.cap.core.production.application.port.in.CreateProductionBatchCommand;
import org.craft.mcs.cap.core.production.application.port.in.CreateProductionBatchUseCase;
import org.craft.mcs.cap.core.production.application.port.in.ProductionBatchCreated;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchRepository;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComponent;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.requireNonNull;


/**
 * Creates a production batch from an already accepted production input.
 *
 * <p>Composition calculation, optimization and target scoring happen before this use case. This
 * service only validates source ownership/stock, consumes feedstock volume atomically and persists
 * the committed production batch. Traceability can be appended here because this is the boundary
 * where accepted material officially enters production.
 */
@Component
@RequiredArgsConstructor
public class CreateProductionBatchService
    implements CreateProductionBatchUseCase {

  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull ProductionBatchRepository productionBatchRepository;

  @Override
  @Transactional
  public @NonNull ProductionBatchCreated create(@NonNull CreateProductionBatchCommand command) {
    requireNonNull(command, "create production batch command cannot be null");

    final var batches = loadAndValidateBatches(command);
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

    consumeFeedstockVolumes(command, batches);
    final var savedProductionBatch = productionBatchRepository.save(productionBatch);

    return new ProductionBatchCreated(savedProductionBatch, input);
  }

  private @NonNull ArrayList<@NonNull FeedstockBatch> loadAndValidateBatches(
      @NonNull CreateProductionBatchCommand command
  ) {
    final var sources = command.input().sourceComposition().sources();
    final var batches = new ArrayList<FeedstockBatch>(sources.size());

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

  private void consumeFeedstockVolumes(
      @NonNull CreateProductionBatchCommand command,
      @NonNull ArrayList<@NonNull FeedstockBatch> batches
  ) {
    final var sources = command.input().sourceComposition().sources();

    for (int i = 0; i < sources.size(); i++) {
      final var batch = batches.get(i);
      final ProductionSourceComponent source = sources.get(i);
      batch.consumeAvailableVolume(source.volume());
      feedstockBatchRepository.save(batch);
    }
  }
}
