package org.craft.mcs.cap.core.production.domain.model;

import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionTargetDistance;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Accepted material input for creating a production batch.
 *
 * <p>This value object represents a decision that has already happened before production starts:
 * the source volumes were selected, their resulting profile was calculated or provided, and the
 * technologist accepted that input. Creating a {@link ProductionBatch} consumes this input and does
 * not run composition or optimization algorithms itself.
 */
@ValueObject
public record ProductionInput(
    @NonNull ProductionSourceComposition sourceComposition,
    @NonNull ProductionProfileSnapshot profile,
    @Nullable ProductionTargetDistance targetDistance
) {

  public ProductionInput {
    requireNonNull(sourceComposition, "production input source composition cannot be null");
    requireNonNull(profile, "production input profile cannot be null");
  }
}
