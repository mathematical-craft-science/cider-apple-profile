package org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain;

import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * One source participating in chemical profile composition calculation.
 *
 * <p>The source is a context-neutral mathematical input: reference, canonical characteristics,
 * weight, and optional availability bound. Feedstock batches, manual lab entries, and future
 * sources can all be projected into this shape without coupling the calculator to those contexts.
 */
public record ChemicalProfileCompositionSource(
    @NonNull ChemicalProfileCompositionSourceReference reference,
    @NonNull CompositionSourceCharacteristics characteristics,
    @NonNull ChemicalProfileCompositionSourceWeight weight,
    @Nullable VolumeValue availableVolume
) {

  public ChemicalProfileCompositionSource {
    requireNonNull(reference, "chemical profile composition source reference cannot be null");
    requireNonNull(characteristics, "composition source characteristics cannot be null");
    requireNonNull(weight, "chemical profile composition source weight cannot be null");

    if (weight instanceof AbsoluteCompositionVolume(VolumeValue volume)
        && availableVolume != null
        && volume.exceeds(availableVolume)) {
      throw new InvalidChemicalProfileCompositionException(
          "composition source requested volume exceeds available volume: "
              + reference.type() + ":" + reference.id()
      );
    }
  }
}
