package org.craft.mcs.cap.shared.kernel.chemical;

import java.util.Optional;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredCharacteristic;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

@ValueObject
public record ChemicalProfile(
    @NonNull Optional<@NonNull MeasuredCharacteristic<SugarConcentration>> sugar,
    @NonNull Optional<@NonNull MeasuredCharacteristic<PhValue>> ph,
    @NonNull Optional<@NonNull MeasuredCharacteristic<TitratableAcidityValue>> titratableAcidity,
    @NonNull Optional<@NonNull MeasuredCharacteristic<TanninityValue>> tanninity,

    @Nullable ChemicalProfileSource source, @Nullable ConfidenceLevel confidence
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

  public static @NonNull Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private @Nullable MeasuredCharacteristic<SugarConcentration> sugar;
    private @Nullable MeasuredCharacteristic<PhValue> ph;
    private @Nullable MeasuredCharacteristic<TitratableAcidityValue> titratableAcidity;
    private @Nullable MeasuredCharacteristic<TanninityValue> tanninity;

    private @Nullable ChemicalProfileSource source;
    private @Nullable ConfidenceLevel confidence;

    private Builder() {
    }

    public @NonNull Builder sugar(@NonNull MeasuredCharacteristic<SugarConcentration> sugar) {
      this.sugar = requireNonNull(sugar);
      return this;
    }

    public @NonNull Builder ph(@NonNull MeasuredCharacteristic<PhValue> ph) {
      this.ph = requireNonNull(ph);
      return this;
    }

    public @NonNull Builder titratableAcidity(
        @NonNull MeasuredCharacteristic<TitratableAcidityValue> titratableAcidity
    ) {
      this.titratableAcidity = requireNonNull(titratableAcidity);
      return this;
    }

    public @NonNull Builder tanninity(@NonNull MeasuredCharacteristic<TanninityValue> tanninity) {
      this.tanninity = requireNonNull(tanninity);
      return this;
    }

    public @NonNull Builder source(@Nullable ChemicalProfileSource source) {
      this.source = source;
      return this;
    }

    public @NonNull Builder confidence(@Nullable ConfidenceLevel confidence) {
      this.confidence = confidence;
      return this;
    }

    public @NonNull ChemicalProfile build() {
      return new ChemicalProfile(
          Optional.ofNullable(sugar),
          Optional.ofNullable(ph),
          Optional.ofNullable(titratableAcidity), Optional.ofNullable(tanninity), source, confidence
      );
    }
  }
}