package org.craft.mcs.cap.shared.kernel.chemical;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@ValueObject
public record ChemicalProfileSource(@NonNull String value) {

  public ChemicalProfileSource {
    requireNonNull(value, "source cannot be null");
    value = value.strip();

    if (value.isBlank()) {
      throw new IllegalArgumentException("source cannot be blank");
    }

    if (value.length() > 255) {
      throw new IllegalArgumentException("source too long");
    }
  }

  public static @NonNull ChemicalProfileSource of(@NonNull String value) {
    return new ChemicalProfileSource(value);
  }
}
