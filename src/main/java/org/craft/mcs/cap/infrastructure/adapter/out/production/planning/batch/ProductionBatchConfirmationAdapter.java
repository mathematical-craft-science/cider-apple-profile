package org.craft.mcs.cap.infrastructure.adapter.out.production.planning.batch;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.batch.application.creation.port.in.CreateProductionBatchFromPlanCommand;
import org.craft.mcs.cap.core.production.batch.application.creation.port.in.CreateProductionBatchFromPlanUseCase;
import org.craft.mcs.cap.core.production.batch.application.creation.port.in.ProductionSourceEvidenceExpectation;
import org.craft.mcs.cap.core.production.planning.application.confirmation.model.ProductionBatchConfirmationRequest;
import org.craft.mcs.cap.core.production.planning.application.confirmation.model.ProductionBatchConfirmationResult;
import org.craft.mcs.cap.core.production.planning.application.confirmation.port.out.ProductionBatchConfirmationGateway;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.production.planning.batch.ProductionBatchConfirmationFailureTranslator.translate;

/** ACL translating an accepted planning candidate into the production-batch creation language. */
@Component
@RequiredArgsConstructor
public final class ProductionBatchConfirmationAdapter implements ProductionBatchConfirmationGateway {

  private final @NonNull CreateProductionBatchFromPlanUseCase createProductionBatchFromPlanUseCase;

  @Override
  public @NonNull ProductionBatchConfirmationResult confirm(
      @NonNull ProductionBatchConfirmationRequest request
  ) {
    requireNonNull(request, "production batch confirmation request cannot be null");
    final var sourceEvidence = request.sourceEvidence().entrySet().stream()
        .collect(Collectors.toUnmodifiableMap(
            java.util.Map.Entry::getKey,
            entry -> new ProductionSourceEvidenceExpectation(
                entry.getValue().evidenceReference(),
                entry.getValue().materialStateId()
            )
        ));
    return translate(() -> {
      final var created = createProductionBatchFromPlanUseCase.create(new CreateProductionBatchFromPlanCommand(
          request.workspaceId(),
          request.name(),
          request.vesselId(),
          request.sourceComposition(),
          sourceEvidence,
          request.profile(),
          request.note()
      ));
      final var batch = created.batch();
      return new ProductionBatchConfirmationResult(
          batch.batchId(),
          batch.workspaceId(),
          batch.name(),
          created.vessel(),
          batch.lifecycle(),
          batch.alcoholicFermentationState(),
          batch.note()
      );
    });
  }
}
