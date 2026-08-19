package org.craft.mcs.cap.core.appleprofile.catalog.application;

import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyName;
import org.craft.mcs.cap.shared.application.ApplicationConflictException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Raised when the catalog already contains an apple variety with the requested name. */
@SuppressWarnings("java:S110")
public final class AppleVarietyNameConflictException extends ApplicationConflictException {

  public AppleVarietyNameConflictException(
      @NonNull AppleVarietyName name,
      @NonNull Throwable cause
  ) {
    super(
        "apple variety already exists: "
            + requireNonNull(name, "apple variety name cannot be null").value(),
        requireNonNull(cause, "apple variety name conflict cause cannot be null")
    );
  }
}
