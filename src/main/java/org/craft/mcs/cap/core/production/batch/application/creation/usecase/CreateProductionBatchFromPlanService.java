package org.craft.mcs.cap.core.production.batch.application.creation.usecase;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.batch.application.authorization.port.out.ProductionAccessControl;
import org.craft.mcs.cap.core.production.batch.application.creation.port.in.CreateProductionBatchFromPlanCommand;
import org.craft.mcs.cap.core.production.batch.application.creation.port.in.CreateProductionBatchFromPlanUseCase;
import org.craft.mcs.cap.core.production.batch.application.creation.port.in.CreatedProductionBatch;
import org.craft.mcs.cap.core.production.batch.application.port.out.ConsumedProductionSource;
import org.craft.mcs.cap.core.production.batch.application.port.out.ProductionBatchRepository;
import org.craft.mcs.cap.core.production.batch.application.port.out.ProductionBatchTraceRecorder;
import org.craft.mcs.cap.core.production.batch.application.port.out.ProductionSourceInventory;
import org.craft.mcs.cap.core.production.batch.application.usecase.ProductionBatchViewMapper;
import org.craft.mcs.cap.core.production.batch.application.vessel.port.out.ProductionVesselAssignmentValidator;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/** Creates and traces a production batch while preserving source material consistency. */
@Component
@RequiredArgsConstructor
public final class CreateProductionBatchFromPlanService implements CreateProductionBatchFromPlanUseCase {

  private static final @NonNull Comparator<ProductionSourceReference> SOURCE_REFERENCE_ORDER =
      Comparator.comparing(ProductionSourceReference::type)
          .thenComparing(ProductionSourceReference::id);
  private static final @NonNull Comparator<ProductionSourceComponent> SOURCE_CONSUMPTION_ORDER =
      Comparator.comparing(ProductionSourceComponent::reference, SOURCE_REFERENCE_ORDER);

  private final @NonNull ProductionSourceInventory sourceInventory;
  private final @NonNull ProductionBatchRepository productionBatchRepository;
  private final @NonNull ProductionBatchTraceRecorder traceRecorder;
  private final @NonNull ProductionAccessControl productionAccessControl;
  private final @NonNull ProductionVesselAssignmentValidator vesselAssignmentValidator;
  private final @NonNull TransactionHelper transactionHelper;
  private final @NonNull Clock clock;

  @Override
  public @NonNull CreatedProductionBatch create(@NonNull CreateProductionBatchFromPlanCommand command) {
    requireNonNull(command, "create production batch from plan command cannot be null");
    productionAccessControl.requireWriteAccess(command.workspaceId());

    return transactionHelper.callInTransactionNonNull(() -> createAtomically(command));
  }

  private @NonNull CreatedProductionBatch createAtomically(
      @NonNull CreateProductionBatchFromPlanCommand command
  ) {
    final var vessel = vesselAssignmentValidator.requireAssignable(
        command.workspaceId(),
        command.vesselId(),
        command.sourceComposition().totalVolume()
    );
    final var consumedSources = consumeSources(command);
    final var productionBatch = ProductionBatch.create(
        ProductionBatchId.newId(),
        command.workspaceId(),
        command.name(),
        command.vesselId(),
        command.sourceComposition().totalVolume(),
        command.profile(),
        command.note()
    );
    final var savedProductionBatch = productionBatchRepository.create(productionBatch);
    traceRecorder.recordCreated(
        savedProductionBatch,
        consumedSources,
        Instant.now(clock),
        command.note()
    );
    return new CreatedProductionBatch(ProductionBatchViewMapper.toView(savedProductionBatch), vessel);
  }

  private @NonNull List<@NonNull ConsumedProductionSource> consumeSources(
      @NonNull CreateProductionBatchFromPlanCommand command
  ) {
    final var sources = command.sourceComposition().sources().stream()
        .sorted(SOURCE_CONSUMPTION_ORDER)
        .toList();
    final var consumedSources = new ArrayList<ConsumedProductionSource>(sources.size());
    for (final var source : sources) {
      consumedSources.add(sourceInventory.consume(
          command.workspaceId(),
          source,
          command.sourceEvidence().get(source.reference())
      ));
    }
    return List.copyOf(consumedSources);
  }
}
