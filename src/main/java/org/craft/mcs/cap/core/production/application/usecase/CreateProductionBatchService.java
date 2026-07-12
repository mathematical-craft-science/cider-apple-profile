package org.craft.mcs.cap.core.production.application.usecase;

import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.application.port.in.CreateProductionBatchCommand;
import org.craft.mcs.cap.core.production.application.port.in.CreateProductionBatchUseCase;
import org.craft.mcs.cap.core.production.application.port.in.ProductionBatchCreated;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchRepository;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchTraceRecorder;
import org.craft.mcs.cap.core.production.application.port.out.ProductionSourceConsumptionPort;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Creates a production batch from an already accepted production input.
 *
 * <p>Composition calculation, optimization and target scoring happen before this use case. This
 * service delegates source-specific validation, volume accounting and source snapshots to a
 * production-owned outbound port, persists the confirmed production batch, and records the
 * production-created traceability fact.
 */
@Component
@RequiredArgsConstructor
public class CreateProductionBatchService
    implements CreateProductionBatchUseCase {

  private final @NonNull ProductionBatchRepository productionBatchRepository;
  private final @NonNull ProductionSourceConsumptionPort sourceConsumptionPort;
  private final @NonNull ProductionBatchTraceRecorder traceRecorder;
  private final @NonNull TransactionHelper transactionHelper;
  private final @NonNull Clock clock;

  @Override
  public @NonNull ProductionBatchCreated create(@NonNull CreateProductionBatchCommand command) {
    requireNonNull(command, "create production batch command cannot be null");

    return transactionHelper.callInTransactionNonNull(() -> {
      final var input = command.input();
      final var traceSources = sourceConsumptionPort.consumeAndSnapshotSources(
          command.ciderMakerId(),
          input.sourceComposition().sources()
      );
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

      final var savedProductionBatch = productionBatchRepository.save(productionBatch);
      traceRecorder.recordCreated(
          savedProductionBatch,
          traceSources,
          Instant.now(clock),
          command.note()
      );

      return new ProductionBatchCreated(savedProductionBatch, input);
    });
  }
}
