package org.craft.mcs.cap.core.traceability.application.port.in;

import org.jspecify.annotations.NonNull;


/** Queries whether a material subject has participated in any operation after its creation. */
public interface QueryBatchOperationHistoryUseCase {

  boolean hasOperations(@NonNull BatchOperationHistoryQuery query);
}
