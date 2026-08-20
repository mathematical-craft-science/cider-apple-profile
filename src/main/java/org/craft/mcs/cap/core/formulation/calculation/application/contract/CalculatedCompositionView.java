package org.craft.mcs.cap.core.formulation.calculation.application.contract;

import java.util.List;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Published mass-balance result without exposing Formulation domain objects. */
public record CalculatedCompositionView(
    @NonNull List<@NonNull CompositionSourceInput> sources,
    @NonNull Characteristics characteristics,
    @Nullable VolumeValue totalVolume,
    @NonNull List<@NonNull Warning> warnings
) {

  public CalculatedCompositionView {
    sources = List.copyOf(requireNonNull(sources, "calculated composition sources cannot be null"));
    requireNonNull(characteristics, "calculated composition characteristics cannot be null");
    warnings = List.copyOf(requireNonNull(warnings, "calculated composition warnings cannot be null"));
  }

  public record Characteristics(
      @NonNull SpecificGravityValue specificGravity,
      @NonNull TitratableAcidityValue titratableAcidity,
      @Nullable TanninValue tannin,
      @Nullable YeastAssimilableNitrogenValue yeastAssimilableNitrogen
  ) {

    public Characteristics {
      requireNonNull(specificGravity, "calculated specific gravity cannot be null");
      requireNonNull(titratableAcidity, "calculated titratable acidity cannot be null");
    }
  }

  public enum Warning {
    TANNIN_IGNORED,
    TANNIN_INCOMPLETE,
    YEAST_ASSIMILABLE_NITROGEN_INCOMPLETE
  }
}
