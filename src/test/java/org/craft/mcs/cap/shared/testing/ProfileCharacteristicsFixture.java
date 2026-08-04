package org.craft.mcs.cap.shared.testing;

import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/** Test-only typed characteristic fixture for scenarios that need a partial cider profile. */
public record ProfileCharacteristicsFixture(
    @Nullable CharacteristicValue<SpecificGravityValue> specificGravity,
    @Nullable CharacteristicValue<TitratableAcidityValue> titratableAcidity,
    @Nullable CharacteristicValue<PhValue> ph,
    @Nullable CharacteristicValue<TanninValue> tannin,
    @Nullable CharacteristicValue<YeastAssimilableNitrogenValue> yeastAssimilableNitrogen
) {

  public static @NonNull Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private @Nullable CharacteristicValue<SpecificGravityValue> specificGravity;
    private @Nullable CharacteristicValue<TitratableAcidityValue> titratableAcidity;
    private @Nullable CharacteristicValue<PhValue> ph;
    private @Nullable CharacteristicValue<TanninValue> tannin;
    private @Nullable CharacteristicValue<YeastAssimilableNitrogenValue> yeastAssimilableNitrogen;

    public @NonNull Builder specificGravity(
        @NonNull CharacteristicValue<SpecificGravityValue> specificGravity
    ) {
      this.specificGravity = specificGravity;
      return this;
    }

    public @NonNull Builder titratableAcidity(
        @NonNull CharacteristicValue<TitratableAcidityValue> titratableAcidity
    ) {
      this.titratableAcidity = titratableAcidity;
      return this;
    }

    public @NonNull Builder ph(@NonNull CharacteristicValue<PhValue> ph) {
      this.ph = ph;
      return this;
    }

    public @NonNull Builder tannin(@NonNull CharacteristicValue<TanninValue> tannin) {
      this.tannin = tannin;
      return this;
    }

    public @NonNull Builder yeastAssimilableNitrogen(
        @NonNull CharacteristicValue<YeastAssimilableNitrogenValue> yeastAssimilableNitrogen
    ) {
      this.yeastAssimilableNitrogen = yeastAssimilableNitrogen;
      return this;
    }

    public @NonNull ProfileCharacteristicsFixture build() {
      return new ProfileCharacteristicsFixture(
          specificGravity,
          titratableAcidity,
          ph,
          tannin,
          yeastAssimilableNitrogen
      );
    }
  }
}
