package org.craft.mcs.cap.core.production.batch.application.port.in;

import org.craft.mcs.cap.core.production.batch.application.model.ProductionBatchDetailsView;
import org.jspecify.annotations.NonNull;


/** Replaces editable production metadata without performing material operations. */
public interface UpdateProductionBatchUseCase {

  @NonNull ProductionBatchDetailsView update(@NonNull UpdateProductionBatchCommand command);
}
