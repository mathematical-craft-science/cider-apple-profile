package org.craft.mcs.cap.core.production.planning.application.confirmation.port.out;

import org.craft.mcs.cap.core.production.planning.application.confirmation.model.ProductionBatchConfirmationRequest;
import org.craft.mcs.cap.core.production.planning.application.confirmation.model.ProductionBatchConfirmationResult;
import org.jspecify.annotations.NonNull;

/** Consumer-owned boundary for materializing an accepted production candidate. */
public interface ProductionBatchConfirmationGateway {

  @NonNull ProductionBatchConfirmationResult confirm(@NonNull ProductionBatchConfirmationRequest request);
}
