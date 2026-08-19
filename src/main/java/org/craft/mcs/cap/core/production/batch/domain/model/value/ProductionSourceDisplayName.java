package org.craft.mcs.cap.core.production.batch.domain.model.value;

import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Production-owned display-name snapshot for a consumed material source. */
public record ProductionSourceDisplayName(@NonNull String value) {

  private static final int MAX_LENGTH = 255;

  public ProductionSourceDisplayName {
    value = requireNonNull(value, "production source display name cannot be null").strip();

    if (value.isEmpty()) {
      throw new ApplicationValidationException("production source display name cannot be blank");
    }
    if (value.length() > MAX_LENGTH) {
      throw new ApplicationValidationException(
          "production source display name length must not exceed " + MAX_LENGTH + " characters"
      );
    }
  }

  public static @NonNull ProductionSourceDisplayName of(@NonNull String value) {
    return new ProductionSourceDisplayName(value);
  }
}
