package org.craft.mcs.cap.core.batchtraceability.application.model;

import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Initial current-state pointer created for a new material subject by a traceability step. */
public record MaterialStateInitialization(
    @NonNull BatchTraceSubject subject,
    @NonNull MaterialStateId initialStateId
) {

  public MaterialStateInitialization {
    requireNonNull(subject, "initial material state subject cannot be null");
    requireNonNull(initialStateId, "initial material state id cannot be null");
  }
}
