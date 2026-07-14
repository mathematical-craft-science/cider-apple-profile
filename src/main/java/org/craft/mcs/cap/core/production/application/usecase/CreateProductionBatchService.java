package org.craft.mcs.cap.core.production.application.usecase;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.application.authorization.port.out.ProductionAccessControl;
import org.craft.mcs.cap.core.production.application.port.in.CreateProductionBatchCommand;
import org.craft.mcs.cap.core.production.application.port.in.CreateProductionBatchUseCase;
import org.craft.mcs.cap.core.production.application.port.in.ProductionBatchCreated;
import org.craft.mcs.cap.core.production.application.port.out.ConsumedProductionSource;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchRepository;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchFeedstockTraceSource;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchTraceRecorder;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchTraceSource;
import org.craft.mcs.cap.core.production.application.port.out.ProductionSourceInventory;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Creates a production batch from an already accepted production input.
 *
 * <p>Composition calculation, optimization and target scoring happen before this use case. This
 * service only validates source ownership/stock, consumes feedstock volume atomically and persists
 * the confirmed production batch. It then records the production-created traceability fact through
 * a production-owned outbound port, keeping traceability mapping outside this use case.
 */
@Component
@RequiredArgsConstructor
public final class CreateProductionBatchService
    implements CreateProductionBatchUseCase {

  private final @NonNull ProductionSourceInventory sourceInventory;
  private final @NonNull ProductionBatchRepository productionBatchRepository;
  private final @NonNull ProductionBatchTraceRecorder traceRecorder;
  private final @NonNull ProductionAccessControl productionAccessControl;
  private final @NonNull TransactionHelper transactionHelper;
  private final @NonNull Clock clock;

  @Override
  public @NonNull ProductionBatchCreated create(@NonNull CreateProductionBatchCommand command) {
    requireNonNull(command, "create production batch command cannot be null");
    productionAccessControl.requireWriteAccess(command.workspaceId());

    return transactionHelper.callInTransactionNonNull(() -> {
      final var input = command.input();
      final var consumedSources = consumeSources(command);
      final var productionBatch = ProductionBatch.create(
          ProductionBatchId.newId(),
          command.workspaceId(),
          command.name(),
          command.kind(),
          input.sourceComposition().totalVolume(),
          input.profile(),
          input.targetDistance(),
          command.note()
      );

      final var savedProductionBatch = productionBatchRepository.save(productionBatch);
      traceRecorder.recordCreated(
          savedProductionBatch,
          traceSources(consumedSources),
          Instant.now(clock),
          command.note()
      );

      return new ProductionBatchCreated(savedProductionBatch, input);
    });
  }

  private @NonNull List<@NonNull ConsumedProductionSource> consumeSources(
      @NonNull CreateProductionBatchCommand command
  ) {
    final var sources = command.input().sourceComposition().sources();
    final var consumedSources = new ArrayList<ConsumedProductionSource>(sources.size());

    for (final var source : sources) {
      consumedSources.add(sourceInventory.consume(command.workspaceId(), source));
    }

    return List.copyOf(consumedSources);
  }

  private static @NonNull List<@NonNull ProductionBatchTraceSource> traceSources(
      @NonNull List<@NonNull ConsumedProductionSource> consumedSources
  ) {
    final var traceSources = new ArrayList<ProductionBatchTraceSource>(consumedSources.size());

    for (final var source : consumedSources) {
      traceSources.add(toTraceSource(source));
    }

    return List.copyOf(traceSources);
  }

  private static @NonNull ProductionBatchTraceSource toTraceSource(
      @NonNull ConsumedProductionSource source
  ) {
    requireNonNull(source, "consumed production source cannot be null");

    return switch (source.reference().type()) {
      case FEEDSTOCK_BATCH -> new ProductionBatchFeedstockTraceSource(
          FeedstockBatchId.of(source.reference().id()),
          source.displayName(),
          source.volume(),
          source.profile(),
          source.confidence()
      );
    };
  }
}
