package org.craft.mcs.cap.core.production.candidate.application.confirmation.usecase;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.application.authorization.port.out.ProductionAccessControl;
import org.craft.mcs.cap.core.production.application.port.out.ConsumedProductionSource;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchRepository;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchTraceRecorder;
import org.craft.mcs.cap.core.production.application.port.out.ProductionSourceInventory;
import org.craft.mcs.cap.core.production.application.usecase.ProductionBatchViewMapper;
import org.craft.mcs.cap.core.production.application.vessel.port.out.ProductionVesselAssignmentValidator;
import org.craft.mcs.cap.core.production.candidate.application.confirmation.port.in.CreateProductionBatchCommand;
import org.craft.mcs.cap.core.production.candidate.application.confirmation.port.in.CreateProductionBatchUseCase;
import org.craft.mcs.cap.core.production.candidate.application.confirmation.port.in.ProductionBatchCreated;
import org.craft.mcs.cap.core.production.candidate.application.ProductionBatchCandidateNotFoundException;
import org.craft.mcs.cap.core.production.candidate.application.ProductionBatchCandidateOwnershipMismatchException;
import org.craft.mcs.cap.core.production.candidate.application.ProductionBatchCandidateStaleException;
import org.craft.mcs.cap.core.production.candidate.application.model.ProductionBatchCandidateSnapshot;
import org.craft.mcs.cap.core.production.candidate.application.port.out.ProductionBatchCandidateStore;
import org.craft.mcs.cap.core.production.candidate.application.usecase.ProductionBatchCandidateViewMapper;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionBatchCandidate;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComposition;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Creates a production batch from an already accepted source selection.
 *
 * <p>Composition calculation, optimization and target scoring happen before this use case. This
 * service validates candidate ownership and source evidence, consumes source volume atomically and
 * creates the batch from the accepted candidate profile. It then records the production-created
 * traceability fact and confirms the candidate in the same transaction.
 */
@Component
@RequiredArgsConstructor
public final class CreateProductionBatchService
    implements CreateProductionBatchUseCase {

  private static final @NonNull Comparator<ProductionSourceReference> SOURCE_REFERENCE_ORDER =
      Comparator.comparing(ProductionSourceReference::type)
          .thenComparing(ProductionSourceReference::id);
  private static final @NonNull Comparator<ProductionSourceComponent> SOURCE_CONSUMPTION_ORDER =
      Comparator.comparing(ProductionSourceComponent::reference, SOURCE_REFERENCE_ORDER);

  private final @NonNull ProductionSourceInventory sourceInventory;
  private final @NonNull ProductionBatchCandidateStore candidateStore;
  private final @NonNull ProductionBatchRepository productionBatchRepository;
  private final @NonNull ProductionBatchTraceRecorder traceRecorder;
  private final @NonNull ProductionAccessControl productionAccessControl;
  private final @NonNull ProductionVesselAssignmentValidator vesselAssignmentValidator;
  private final @NonNull TransactionHelper transactionHelper;
  private final @NonNull Clock clock;

  @Override
  public @NonNull ProductionBatchCreated create(@NonNull CreateProductionBatchCommand command) {
    requireNonNull(command, "create production batch command cannot be null");
    productionAccessControl.requireWriteAccess(command.workspaceId());

    return transactionHelper.callInTransactionNonNull(() -> {
      final var snapshot = requireCandidate(command);
      final var candidate = snapshot.candidate();
      candidate.requireAvailable();
      final var vessel = vesselAssignmentValidator.requireAssignable(
          command.workspaceId(),
          command.vesselId(),
          candidate.sourceComposition().totalVolume()
      );
      final var consumedSources = consumeSources(command.workspaceId(), candidate.sourceComposition());
      requireCurrentEvidence(candidate, consumedSources);
      final var productionBatch = ProductionBatch.create(
          ProductionBatchId.newId(),
          command.workspaceId(),
          command.name(),
          command.vesselId(),
          candidate.sourceComposition().totalVolume(),
          candidate.profile(),
          command.note()
      );

      final var savedProductionBatch = productionBatchRepository.create(productionBatch);
      traceRecorder.recordCreated(
          savedProductionBatch,
          consumedSources,
          Instant.now(clock),
          command.note()
      );
      candidate.confirm(savedProductionBatch.batchId());
      final var confirmedCandidate = candidateStore.update(snapshot.withCandidate(candidate));

      return new ProductionBatchCreated(
          ProductionBatchViewMapper.toView(savedProductionBatch),
          vessel,
          ProductionBatchCandidateViewMapper.toView(confirmedCandidate)
      );
    });
  }

  private @NonNull ProductionBatchCandidateSnapshot requireCandidate(
      @NonNull CreateProductionBatchCommand command
  ) {
    final var snapshot = candidateStore.findById(command.candidateId())
        .orElseThrow(() -> new ProductionBatchCandidateNotFoundException(command.candidateId()));
    if (!snapshot.candidate().workspaceId().equals(command.workspaceId())) {
      throw new ProductionBatchCandidateOwnershipMismatchException(
          command.workspaceId(),
          snapshot.candidate().workspaceId()
      );
    }
    return snapshot;
  }

  private @NonNull List<@NonNull ConsumedProductionSource> consumeSources(
      @NonNull WorkspaceId workspaceId,
      @NonNull ProductionSourceComposition sourceComposition
  ) {
    final var sources = sourceComposition.sources().stream()
        .sorted(SOURCE_CONSUMPTION_ORDER)
        .toList();
    final var consumedSources = new ArrayList<ConsumedProductionSource>(sources.size());

    for (final var source : sources) {
      consumedSources.add(sourceInventory.consume(workspaceId, source));
    }

    return List.copyOf(consumedSources);
  }

  private static void requireCurrentEvidence(
      @NonNull ProductionBatchCandidate candidate,
      @NonNull List<@NonNull ConsumedProductionSource> consumedSources
  ) {
    for (final var consumedSource : consumedSources) {
      if (!candidate.evidenceFor(consumedSource.reference()).equals(consumedSource.evidenceReference())
          || !candidate.materialStateFor(consumedSource.reference()).equals(consumedSource.sourceStateId())) {
        throw new ProductionBatchCandidateStaleException(consumedSource.reference());
      }
    }
  }

}
