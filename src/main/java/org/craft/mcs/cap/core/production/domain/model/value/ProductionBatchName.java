package org.craft.mcs.cap.core.production.domain.model.value;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@ValueObject
public record ProductionBatchName(@NonNull String value) {

  private static final int MAX_LENGTH = 160;

  public ProductionBatchName {
    requireNonNull(value, "production batch name cannot be null");
    value = value.strip();
    if (value.isEmpty()) {
      throw new IllegalArgumentException("production batch name cannot be blank");
    }
    if (value.length() > MAX_LENGTH) {
      throw new IllegalArgumentException("production batch name cannot be longer than " + MAX_LENGTH);
    }
  }

  public static @NonNull ProductionBatchName of(@NonNull String value) {
    return new ProductionBatchName(value);
  }
}
