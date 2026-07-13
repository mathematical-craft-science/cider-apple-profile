package org.craft.mcs.cap.core.production.application.authorization.port.out;

import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.jspecify.annotations.NonNull;


/** Outbound authorization boundary for production data owned by a cider-maker scope. */
public interface ProductionAccessControl {

  void requireReadAccess(@NonNull CiderMakerId ciderMakerId);

  void requireWriteAccess(@NonNull CiderMakerId ciderMakerId);
}
