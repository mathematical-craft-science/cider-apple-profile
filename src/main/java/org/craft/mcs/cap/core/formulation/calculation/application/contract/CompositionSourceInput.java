package org.craft.mcs.cap.core.formulation.calculation.application.contract;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Published immutable source for composition calculations. */
public record CompositionSourceInput(
    @NonNull Reference reference,
    @NonNull Characteristics characteristics,
    @NonNull Weight weight,
    @Nullable VolumeValue availableVolume
) {

  public CompositionSourceInput {
    requireNonNull(reference, "composition source reference cannot be null");
    requireNonNull(characteristics, "composition source characteristics cannot be null");
    requireNonNull(weight, "composition source weight cannot be null");
  }

  public record Reference(@NonNull String type, @NonNull String id) {

    public Reference {
      requireNonNull(type, "composition source type cannot be null");
      requireNonNull(id, "composition source id cannot be null");
    }
  }

  public record Characteristics(
      @NonNull CharacteristicValue<SpecificGravityValue> specificGravity,
      @NonNull CharacteristicValue<TitratableAcidityValue> titratableAcidity,
      @Nullable CharacteristicValue<TanninValue> tannin,
      @Nullable CharacteristicValue<YeastAssimilableNitrogenValue> yeastAssimilableNitrogen
  ) {

    public Characteristics {
      requireNonNull(specificGravity, "composition source specific gravity cannot be null");
      requireNonNull(titratableAcidity, "composition source titratable acidity cannot be null");
    }
  }

  public sealed interface Weight permits AbsoluteVolume, Proportion {
  }

  public record AbsoluteVolume(@NonNull VolumeValue volume) implements Weight {

    public AbsoluteVolume {
      requireNonNull(volume, "composition source volume cannot be null");
    }
  }

  public record Proportion(@NonNull BigDecimal value) implements Weight {

    public Proportion {
      requireNonNull(value, "composition source proportion cannot be null");
    }
  }
}
