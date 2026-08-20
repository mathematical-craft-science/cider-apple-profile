package org.craft.mcs.cap.core.formulation.calculation.domain;

import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * One source participating in composition calculation.
 *
 * <p>The source is a context-neutral mathematical input: reference, canonical characteristics,
 * weight, and optional availability bound. Feedstock batches, manual lab entries, and future
 * sources can all be projected into this shape without coupling the calculator to those contexts.
 */
public record CompositionSource(
    @NonNull CompositionSourceReference reference,
    @NonNull CompositionSourceCharacteristics characteristics,
    @NonNull CompositionSourceWeight weight,
    @Nullable VolumeValue availableVolume
) {

  public CompositionSource {
    requireNonNull(reference, "composition source reference cannot be null");
    requireNonNull(characteristics, "composition source characteristics cannot be null");
    requireNonNull(weight, "composition source weight cannot be null");

    if (weight instanceof AbsoluteCompositionVolume(VolumeValue volume)
        && availableVolume != null
        && volume.exceeds(availableVolume)) {
      throw new InvalidCompositionException(
          "composition source requested volume exceeds available volume: "
              + reference.type() + ":" + reference.id()
      );
    }
  }
}
