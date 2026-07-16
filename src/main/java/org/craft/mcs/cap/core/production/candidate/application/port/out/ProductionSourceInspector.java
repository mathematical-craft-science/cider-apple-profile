package org.craft.mcs.cap.core.production.candidate.application.port.out;

import org.craft.mcs.cap.core.production.candidate.application.model.AvailableProductionSource;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Outbound boundary for reading authoritative source evidence without consuming material. */
public interface ProductionSourceInspector {

  @NonNull AvailableProductionSource inspect(
      @NonNull WorkspaceId workspaceId,
      @NonNull ProductionSourceComponent source
  );
}
