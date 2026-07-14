package org.craft.mcs.cap.core.production.application.port.out;

import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/**
 * Outbound boundary for validating and consuming material owned by another bounded context.
 *
 * <p>Implementations call each source context through its public inbound boundary and translate
 * the consumed material result into the production-owned source model.
 */
public interface ProductionSourceInventory {

  @NonNull
  ConsumedProductionSource consume(
      @NonNull WorkspaceId workspaceId,
      @NonNull ProductionSourceComponent source
  );
}
