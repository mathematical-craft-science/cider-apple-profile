package org.craft.mcs.cap.shared.kernel.sugar;

import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/**
 * Transitional ChemicalProfile carrier for Specific Gravity.
 *
 * <p>Despite its legacy name, this value does not represent a direct sugar concentration and must
 * not be used as a sensory-sweetness value. New measurement and calculation models should use
 * {@link SpecificGravityValue} directly. The type remains only until ChemicalProfile consumers are
 * replaced by specialized projections.</p>
 */
@ValueObject
public record SugarConcentration(@NonNull SpecificGravityValue specificGravity)
    implements Comparable<SugarConcentration> {

  public SugarConcentration {
    requireNonNull(specificGravity, "specific gravity cannot be null");
  }

  @NonNull
  public static SugarConcentration ofSpecificGravity(
      @NonNull SpecificGravityValue specificGravity
  ) {
    return new SugarConcentration(specificGravity);
  }

  @Override
  public int compareTo(@NonNull SugarConcentration other) {
    requireNonNull(other, "other sugar concentration cannot be null");
    return specificGravity.compareTo(other.specificGravity);
  }
}
