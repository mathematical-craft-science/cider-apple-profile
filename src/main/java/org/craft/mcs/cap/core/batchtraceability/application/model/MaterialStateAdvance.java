package org.craft.mcs.cap.core.batchtraceability.application.model;

import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Compare-and-set transition of one existing material subject from its expected state to a new state.
 */
public record MaterialStateAdvance(
    @NonNull BatchTraceSubject subject,
    @NonNull MaterialStateId expectedStateId,
    @NonNull MaterialStateId nextStateId
) {

  public MaterialStateAdvance {
    requireNonNull(subject, "material state transition subject cannot be null");
    requireNonNull(expectedStateId, "expected material state id cannot be null");
    requireNonNull(nextStateId, "next material state id cannot be null");
    if (expectedStateId.equals(nextStateId)) {
      throw new ApplicationValidationException("material state transition must create a new state");
    }
  }
}
