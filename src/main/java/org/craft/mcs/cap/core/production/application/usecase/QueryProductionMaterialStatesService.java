package org.craft.mcs.cap.core.production.application.usecase;

import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.application.ProductionBatchNotFoundException;
import org.craft.mcs.cap.core.production.application.authorization.port.out.ProductionAccessControl;
import org.craft.mcs.cap.core.production.application.port.in.QueryProductionMaterialStatesUseCase;
import org.craft.mcs.cap.core.production.application.port.out.ProductionMaterialStateQuery;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Resolves authoritative Production material states within an authorized workspace. */
@Component
@RequiredArgsConstructor
public final class QueryProductionMaterialStatesService implements QueryProductionMaterialStatesUseCase {

  private final @NonNull ProductionAccessControl accessControl;
  private final @NonNull ProductionMaterialStateQuery materialStateQuery;

  @Override
  public @NonNull Map<@NonNull ProductionBatchId, @NonNull MaterialStateId> findCurrentStates(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull ProductionBatchId> batchIds
  ) {
    requireNonNull(workspaceId, "production workspace id cannot be null");
    requireNonNull(batchIds, "production batch ids cannot be null");
    accessControl.requireReadAccess(workspaceId);
    if (batchIds.isEmpty()) {
      return Map.of();
    }

    final var states = materialStateQuery.findCurrentStates(workspaceId, Set.copyOf(batchIds));
    batchIds.stream()
        .filter(batchId -> !states.containsKey(batchId))
        .findFirst()
        .ifPresent(batchId -> {
          throw new ProductionBatchNotFoundException(batchId);
        });
    return states;
  }
}
