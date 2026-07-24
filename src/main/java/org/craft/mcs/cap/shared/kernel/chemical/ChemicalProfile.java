package org.craft.mcs.cap.shared.kernel.chemical;

import java.util.Optional;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Immutable chemical projection used by analysis, calculations, and historical snapshots.
 *
 * <p>Measurement evidence and current-value selection remain outside this value object.</p>
 */
@ValueObject
public record ChemicalProfile(
    @NonNull Optional<@NonNull CharacteristicValue<SugarConcentration>> sugar,
    @NonNull Optional<@NonNull CharacteristicValue<PhValue>> ph,
    @NonNull Optional<@NonNull CharacteristicValue<TitratableAcidityValue>> titratableAcidity,
    @NonNull Optional<@NonNull CharacteristicValue<TanninValue>> tannin,
    @NonNull Optional<@NonNull CharacteristicValue<YeastAssimilableNitrogenValue>> yeastAssimilableNitrogen
) {

  public ChemicalProfile {
    requireNonNull(sugar, "sugar characteristic cannot be null");
    requireNonNull(ph, "pH characteristic cannot be null");
    requireNonNull(titratableAcidity, "titratable acidity characteristic cannot be null");
    requireNonNull(tannin, "tannin characteristic cannot be null");
    requireNonNull(yeastAssimilableNitrogen, "YAN characteristic cannot be null");

    if (sugar.isEmpty()
        && ph.isEmpty()
        && titratableAcidity.isEmpty()
        && tannin.isEmpty()
        && yeastAssimilableNitrogen.isEmpty()) {
      throw new DomainRuleViolationException("chemical profile must contain at least one characteristic");
    }
  }

  public static @NonNull Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private @Nullable CharacteristicValue<SugarConcentration> sugar;
    private @Nullable CharacteristicValue<PhValue> ph;
    private @Nullable CharacteristicValue<TitratableAcidityValue> titratableAcidity;
    private @Nullable CharacteristicValue<TanninValue> tannin;
    private @Nullable CharacteristicValue<YeastAssimilableNitrogenValue> yeastAssimilableNitrogen;

    private Builder() {
    }

    public @NonNull Builder sugar(@NonNull CharacteristicValue<SugarConcentration> sugar) {
      this.sugar = requireNonNull(sugar);
      return this;
    }

    public @NonNull Builder ph(@NonNull CharacteristicValue<PhValue> ph) {
      this.ph = requireNonNull(ph);
      return this;
    }

    public @NonNull Builder titratableAcidity(
        @NonNull CharacteristicValue<TitratableAcidityValue> titratableAcidity
    ) {
      this.titratableAcidity = requireNonNull(titratableAcidity);
      return this;
    }

    public @NonNull Builder tannin(@NonNull CharacteristicValue<TanninValue> tannin) {
      this.tannin = requireNonNull(tannin);
      return this;
    }

    public @NonNull Builder yeastAssimilableNitrogen(
        @NonNull CharacteristicValue<YeastAssimilableNitrogenValue> yeastAssimilableNitrogen
    ) {
      this.yeastAssimilableNitrogen = requireNonNull(yeastAssimilableNitrogen);
      return this;
    }

    public @NonNull ChemicalProfile build() {
      return new ChemicalProfile(
          Optional.ofNullable(sugar),
          Optional.ofNullable(ph),
          Optional.ofNullable(titratableAcidity),
          Optional.ofNullable(tannin),
          Optional.ofNullable(yeastAssimilableNitrogen)
      );
    }
  }
}
