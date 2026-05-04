package org.craft.mcs.cap.appleprofile.domain.model;

import java.util.Optional;
import org.craft.mcs.cap.shared.domain.measurement.value.PhValue;
import org.craft.mcs.cap.shared.domain.measurement.value.TanninityValue;
import org.craft.mcs.cap.shared.domain.measurement.value.TitratableAcidityValue;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

@ValueObject
public record ChemicalProfile(
    @NonNull Optional<@NonNull MeasuredCharacteristic<SugarConcentration>> sugar,
    @NonNull Optional<@NonNull MeasuredCharacteristic<PhValue>> ph,
    @NonNull Optional<@NonNull MeasuredCharacteristic<TitratableAcidityValue>> titratableAcidity,
    @NonNull Optional<@NonNull MeasuredCharacteristic<TanninityValue>> tanninity
) {

  public ChemicalProfile {
    requireNonNull(sugar, "sugar characteristic cannot be null");
    requireNonNull(ph, "pH characteristic cannot be null");
    requireNonNull(titratableAcidity, "titratable acidity characteristic cannot be null");
    requireNonNull(tanninity, "tanninity characteristic cannot be null");

    if (sugar.isEmpty() && ph.isEmpty() && titratableAcidity.isEmpty() && tanninity.isEmpty()) {
      throw new IllegalArgumentException("chemical profile must contain at least one characteristic");
    }
  }

  @NonNull
  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    @Nullable
    private MeasuredCharacteristic<SugarConcentration> sugar;

    @Nullable
    private MeasuredCharacteristic<PhValue> ph;

    @Nullable
    private MeasuredCharacteristic<TitratableAcidityValue> titratableAcidity;

    @Nullable
    private MeasuredCharacteristic<TanninityValue> tanninity;

    @NonNull
    public Builder sugar(@NonNull MeasuredCharacteristic<SugarConcentration> sugar) {
      this.sugar = requireNonNull(sugar, "sugar characteristic cannot be null");
      return this;
    }

    @NonNull
    public Builder ph(@NonNull MeasuredCharacteristic<PhValue> ph) {
      this.ph = requireNonNull(ph, "pH characteristic cannot be null");
      return this;
    }

    @NonNull
    public Builder titratableAcidity(
        @NonNull MeasuredCharacteristic<TitratableAcidityValue> titratableAcidity
    ) {
      this.titratableAcidity =
          requireNonNull(titratableAcidity, "titratable acidity characteristic cannot be null");
      return this;
    }

    @NonNull
    public Builder tanninity(@NonNull MeasuredCharacteristic<TanninityValue> tanninity) {
      this.tanninity = requireNonNull(tanninity, "tanninity characteristic cannot be null");
      return this;
    }

    @NonNull
    public ChemicalProfile build() {
      return new ChemicalProfile(
          Optional.ofNullable(sugar),
          Optional.ofNullable(ph),
          Optional.ofNullable(titratableAcidity),
          Optional.ofNullable(tanninity)
      );
    }
  }
}
