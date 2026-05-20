package org.craft.mcs.cap.shared.application;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Application conflict raised when a resource was changed by another transaction before the current
 * command could persist its changes.
 */
@SuppressWarnings("java:S110")
public final class ConcurrentResourceModificationException extends ApplicationConflictException {

  @java.io.Serial
  private static final long serialVersionUID = 1L;

  private final @NonNull String resourceName;

  public ConcurrentResourceModificationException(
      @NonNull String resourceName,
      @Nullable Throwable cause
  ) {
    super(
        "resource was modified concurrently: " + requireNonNull(resourceName, "resource name cannot be null"),
        cause
    );
    this.resourceName = resourceName;
  }

  public @NonNull String resourceName() {
    return resourceName;
  }
}
