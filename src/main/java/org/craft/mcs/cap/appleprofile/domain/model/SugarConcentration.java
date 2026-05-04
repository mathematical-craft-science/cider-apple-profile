package org.craft.mcs.cap.appleprofile.domain.model;

import org.craft.mcs.cap.shared.domain.measurement.value.SpecificGravityValue;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

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
