package org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain;

import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionSourceReference;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionSourceCharacteristics;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import static java.util.Objects.requireNonNull;


/**
 * Source candidate available to a composition optimizer.
 *
 * <p>This is not a feedstock aggregate. It is a mathematical input projection: source identity,
 * normalized chemical axes and optional available volume. The reference type/id lets callers map a
 * winning candidate back to FeedstockBatch, manual lab source or another future source type.
 */
public record ChemicalProfileCompositionOptimizationSource(
    @NonNull ChemicalProfileCompositionSourceReference reference,
    @NonNull CompositionSourceCharacteristics characteristics,
    @Nullable VolumeValue availableVolume
) {

  public ChemicalProfileCompositionOptimizationSource {
    requireNonNull(reference, "chemical profile composition optimization source reference cannot be null");
    requireNonNull(characteristics, "composition optimization source characteristics cannot be null");
  }
}
