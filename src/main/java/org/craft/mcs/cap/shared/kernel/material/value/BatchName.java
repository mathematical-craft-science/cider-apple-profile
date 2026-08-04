package org.craft.mcs.cap.shared.kernel.material.value;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Human-readable name of a material batch shared by Feedstock and Production contexts.
 */
@ValueObject
public record BatchName(@NonNull String value) {

  private static final int MAX_LENGTH = 160;

  public BatchName {
    requireNonNull(value, "batch name cannot be null");
    value = value.strip();

    if (value.isBlank()) {
      throw new DomainRuleViolationException("batch name cannot be blank");
    }
    if (value.length() > MAX_LENGTH) {
      throw new DomainRuleViolationException(
          "batch name length must not exceed " + MAX_LENGTH + " characters"
      );
    }
  }

  public static @NonNull BatchName of(@NonNull String value) {
    return new BatchName(value);
  }
}
