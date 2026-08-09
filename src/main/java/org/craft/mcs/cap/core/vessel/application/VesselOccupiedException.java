package org.craft.mcs.cap.core.vessel.application;

import org.craft.mcs.cap.core.vessel.application.model.VesselCurrentContentView;
import org.craft.mcs.cap.shared.application.ApplicationConflictException;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Raised when an operation requires an empty vessel but active material already occupies it. */
@SuppressWarnings("java:S110")
public final class VesselOccupiedException extends ApplicationConflictException {

  public VesselOccupiedException(
      @NonNull VesselId vesselId,
      @NonNull VesselCurrentContentView currentContent
  ) {
    super("vessel %s is occupied by %s %s".formatted(
        requireNonNull(vesselId, "vessel id cannot be null").value(),
        requireNonNull(currentContent, "current vessel content cannot be null").subjectType(),
        currentContent.subjectId()
    ));
  }
}
