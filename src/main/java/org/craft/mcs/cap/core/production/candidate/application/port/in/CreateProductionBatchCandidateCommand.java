package org.craft.mcs.cap.core.production.candidate.application.port.in;

import org.craft.mcs.cap.core.production.candidate.application.model.ProductionBatchCandidateCalculationRequest;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComposition;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Command to assess and save an authoritative production candidate. */
public record CreateProductionBatchCandidateCommand(
    @NonNull WorkspaceId workspaceId,
    @NonNull ProductionSourceComposition sourceComposition,
    @NonNull ProductionBatchCandidateCalculationRequest calculationRequest
) {

  public CreateProductionBatchCandidateCommand {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(sourceComposition, "production source composition cannot be null");
    requireNonNull(calculationRequest, "production candidate calculation request cannot be null");

    final var sources = sourceComposition.sources();
    if (sources.stream().map(ProductionSourceComponent::reference).distinct().count() != sources.size()) {
      throw new ApplicationValidationException("production batch candidate cannot contain duplicate sources");
    }
  }
}
