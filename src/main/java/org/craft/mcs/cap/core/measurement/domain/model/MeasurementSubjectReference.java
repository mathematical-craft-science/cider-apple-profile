package org.craft.mcs.cap.core.measurement.domain.model;

import java.util.UUID;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Provider-neutral reference to a measured aggregate.
 *
 * <p>The reference deliberately does not depend on Feedstock or Production domain identifiers.
 */
@ValueObject
public record MeasurementSubjectReference(
    @NonNull MeasurementSubjectType type,
    @NonNull UUID id
) {

  public MeasurementSubjectReference {
    requireNonNull(type, "measurement subject type cannot be null");
    requireNonNull(id, "measurement subject id cannot be null");
  }
}
