package org.craft.mcs.cap.core.production.application.port.in;

import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionBatchCandidateId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.shared.kernel.material.value.Vessel;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record CreateProductionBatchCommand(
    @NonNull WorkspaceId workspaceId,
    @NonNull BatchName name,
    @NonNull Vessel vessel,
    @NonNull ProductionBatchCandidateId candidateId,
    @Nullable String note
) {

  public CreateProductionBatchCommand {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(name, "production batch name cannot be null");
    requireNonNull(vessel, "production vessel cannot be null");
    requireNonNull(candidateId, "production batch candidate id cannot be null");
  }
}
