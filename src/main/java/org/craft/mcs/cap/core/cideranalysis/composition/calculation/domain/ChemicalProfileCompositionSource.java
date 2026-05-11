package org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain;

import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record ChemicalProfileCompositionSource(
    @NonNull ChemicalProfileCompositionSourceReference reference,
    @NonNull ChemicalProfileAxes axes,
    @NonNull ChemicalProfileCompositionSourceWeight weight,
    @Nullable VolumeValue availableVolume
) {

  public ChemicalProfileCompositionSource {
    requireNonNull(reference, "chemical profile composition source reference cannot be null");
    requireNonNull(axes, "chemical profile composition source axes cannot be null");
    requireNonNull(weight, "chemical profile composition source weight cannot be null");
  }
}
