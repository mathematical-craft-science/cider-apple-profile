package org.craft.mcs.cap.core.cideranalysis.abv.domain;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;

import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Input for ABV prediction strategies.
 *
 * <p>Both values are canonical specific gravity values. The input enforces the physical assumption
 * that predicted final gravity cannot be higher than original gravity for this potential-ABV
 * calculation.
 */
public record AbvPredictionInput(
    @NonNull SpecificGravityValue originalGravity,
    @NonNull SpecificGravityValue finalGravity
) {

  public AbvPredictionInput {
    requireNonNull(originalGravity, "original gravity cannot be null");
    requireNonNull(finalGravity, "final gravity cannot be null");

    if (finalGravity.exceeds(originalGravity)) {
      throw new DomainRuleViolationException("final gravity must not exceed original gravity");
    }
  }
}
