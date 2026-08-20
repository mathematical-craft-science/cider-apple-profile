package org.craft.mcs.cap.core.formulation.optimization.application.contract;

import org.craft.mcs.cap.core.formulation.calculation.application.contract.CompositionSourceInput.Characteristics;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.CompositionSourceInput.Reference;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Immutable source snapshot accepted by Formulation optimization. */
public record OptimizationSourceInput(
    @NonNull Reference reference,
    @NonNull Characteristics characteristics,
    @Nullable VolumeValue availableVolume
) {

  public OptimizationSourceInput {
    requireNonNull(reference, "optimization source reference cannot be null");
    requireNonNull(characteristics, "optimization source characteristics cannot be null");
  }
}
