package org.craft.mcs.cap.core.traceability.application.port.in;

import org.craft.mcs.cap.core.traceability.application.model.MaterialStateSnapshot;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.jspecify.annotations.NonNull;


/** Resolves one immutable historical material state from traceability facts. */
public interface GetMaterialStateUseCase {

  @NonNull MaterialStateSnapshot get(@NonNull MaterialStateId materialStateId);
}
