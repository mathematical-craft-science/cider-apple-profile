package org.craft.mcs.cap.core.feedstock.domain.measurement;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Protects the feedstock-stage requirement that material still contains fermentable extract.
 *
 * <p>The general specific-gravity value also represents fermented cider below water density.
 * Feedstock is narrower: a value at or below 1.000 does not describe sugar-bearing raw material
 * suitable for a new alcoholic fermentation.</p>
 */
public final class FeedstockSpecificGravityPolicy {

  private static final @NonNull SpecificGravityValue MINIMUM_EXCLUSIVE = SpecificGravityValue.of("1.000");

  /**
   * Requires a specific gravity strictly above water density for feedstock material.
   */
  public void validate(@NonNull SpecificGravityValue specificGravity) {
    requireNonNull(specificGravity, "feedstock specific gravity cannot be null");
    if (specificGravity.compareTo(MINIMUM_EXCLUSIVE) <= 0) {
      throw new DomainRuleViolationException("feedstock specific gravity must be greater than 1.000");
    }
  }
}
