package org.craft.mcs.cap.core.batchtraceability.application;

import org.craft.mcs.cap.shared.application.ApplicationConflictException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Reports immutable trace facts that cannot form one continuous state chain per material subject. */
@SuppressWarnings("java:S110")
public final class InconsistentMaterialStateHistoryException extends ApplicationConflictException {

  public InconsistentMaterialStateHistoryException(@NonNull String reason) {
    super("Cannot rebuild current material states: " + requireNonNull(reason, "reason cannot be null"));
  }
}
