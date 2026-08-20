package org.craft.mcs.cap.core.formulation.calculation.domain;

import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Context-neutral identity of a source participating in a composition scenario. */
public record CompositionSourceReference(
    @NonNull String type,
    @NonNull String id
) {

  public CompositionSourceReference {
    requireNonNull(type, "composition source reference type cannot be null");
    requireNonNull(id, "composition source reference id cannot be null");

    type = type.strip();
    id = id.strip();

    if (type.isEmpty()) {
      throw new InvalidCompositionException(
          "composition source reference type cannot be blank"
      );
    }
    if (id.isEmpty()) {
      throw new InvalidCompositionException(
          "composition source reference id cannot be blank"
      );
    }
  }
}
