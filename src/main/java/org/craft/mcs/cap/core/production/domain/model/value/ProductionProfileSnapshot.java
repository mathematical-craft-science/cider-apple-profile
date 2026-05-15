package org.craft.mcs.cap.core.production.domain.model.value;

import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


@ValueObject
public record ProductionProfileSnapshot(
    @NonNull SpecificGravityValue sugar,
    @NonNull TitratableAcidityValue acidity,
    @Nullable TanninityValue tannin
) {

  public ProductionProfileSnapshot {
    requireNonNull(sugar, "production sugar cannot be null");
    requireNonNull(acidity, "production acidity cannot be null");
  }
}
