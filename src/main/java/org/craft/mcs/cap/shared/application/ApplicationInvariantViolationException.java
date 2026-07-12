package org.craft.mcs.cap.shared.application;

import org.jspecify.annotations.NonNull;


/**
 * Application exception for violated use-case postconditions or internal application invariants.
 *
 * <p>Use this when a previous capability check, repository lookup, or query contract guarantees a
 * value, but the value is unexpectedly absent. It keeps core services free from technical
 * {@link IllegalStateException} while still making the defect explicit.
 */
public final class ApplicationInvariantViolationException extends ApplicationException {

  @java.io.Serial
  private static final long serialVersionUID = 1L;

  public ApplicationInvariantViolationException(@NonNull String message) {
    super(message);
  }
}
