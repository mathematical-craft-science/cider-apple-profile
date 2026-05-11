package org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain;

import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record ChemicalProfileCompositionSourceReference(
    @NonNull String type,
    @NonNull String id
) {

  public ChemicalProfileCompositionSourceReference {
    requireNonNull(type, "chemical profile composition source reference type cannot be null");
    requireNonNull(id, "chemical profile composition source reference id cannot be null");

    type = type.strip();
    id = id.strip();

    if (type.isEmpty()) {
      throw new IllegalArgumentException("chemical profile composition source reference type cannot be blank");
    }
    if (id.isEmpty()) {
      throw new IllegalArgumentException("chemical profile composition source reference id cannot be blank");
    }
  }
}
