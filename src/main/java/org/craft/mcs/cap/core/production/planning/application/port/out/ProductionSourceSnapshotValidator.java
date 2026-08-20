package org.craft.mcs.cap.core.production.planning.application.port.out;

import org.craft.mcs.cap.core.production.planning.application.contract.AvailableProductionSource;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Revalidates one inspected source under the source owner's aggregate lock. */
public interface ProductionSourceSnapshotValidator {

  void validate(
      @NonNull WorkspaceId workspaceId,
      @NonNull AvailableProductionSource source
  );
}
