package org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Absolute source weight expressed as available or planned volume.
 *
 * <p>This weight mode is used when a user wants the calculation or optimization to produce concrete
 * liters that can later be committed into a production batch.
 */
public record AbsoluteCompositionVolume(@NonNull VolumeValue volume) implements ChemicalProfileCompositionSourceWeight {

  public AbsoluteCompositionVolume {
    requireNonNull(volume, "absolute composition volume cannot be null");

    if (volume.isZero()) {
      throw new IllegalArgumentException("absolute composition volume must be greater than zero");
    }
  }

  @Override
  public @NonNull BigDecimal value() {
    return volume.liters();
  }
}
