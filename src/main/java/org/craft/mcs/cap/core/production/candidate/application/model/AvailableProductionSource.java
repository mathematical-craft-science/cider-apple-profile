package org.craft.mcs.cap.core.production.candidate.application.model;

import org.craft.mcs.cap.core.production.application.port.out.ProductionSourceDisplayName;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionSourceEvidenceReference;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Authoritative source snapshot used to assess a production candidate without consuming material. */
public record AvailableProductionSource(
    @NonNull ProductionSourceReference reference,
    @Nullable ProductionSourceDisplayName displayName,
    @NonNull VolumeValue selectedVolume,
    @NonNull VolumeValue availableVolume,
    @NonNull ChemicalProfileAxes profile,
    @Nullable ConfidenceLevel confidence,
    @NonNull ProductionSourceEvidenceReference evidenceReference
) {

  public AvailableProductionSource {
    requireNonNull(reference, "production source reference cannot be null");
    requireNonNull(selectedVolume, "selected production source volume cannot be null");
    requireNonNull(availableVolume, "available production source volume cannot be null");
    requireNonNull(profile, "available production source profile cannot be null");
    requireNonNull(evidenceReference, "production source evidence reference cannot be null");
  }
}
