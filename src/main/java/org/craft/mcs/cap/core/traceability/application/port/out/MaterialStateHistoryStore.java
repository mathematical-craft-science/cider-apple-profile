package org.craft.mcs.cap.core.traceability.application.port.out;

import java.util.Optional;
import org.craft.mcs.cap.core.traceability.application.model.MaterialStateSnapshot;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.jspecify.annotations.NonNull;


/** Persistence boundary for immutable material-state snapshots recorded in trace history. */
public interface MaterialStateHistoryStore {

  @NonNull Optional<@NonNull MaterialStateSnapshot> find(
      @NonNull MaterialStateId materialStateId
  );
}
