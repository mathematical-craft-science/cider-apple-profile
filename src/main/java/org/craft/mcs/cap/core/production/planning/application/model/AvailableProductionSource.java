package org.craft.mcs.cap.core.production.planning.application.model;

import org.craft.mcs.cap.core.production.batch.domain.model.value.ProductionSourceCharacteristics;
import org.craft.mcs.cap.core.production.batch.domain.model.value.ProductionSourceDisplayName;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceEvidenceReference;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.material.value.MaterialAvailability;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Authoritative source snapshot used to assess a production candidate without consuming material. */
public record AvailableProductionSource(
    @NonNull ProductionSourceReference reference,
    @Nullable ProductionSourceDisplayName displayName,
    @NonNull MaterialAvailability availability,
    @NonNull ProductionSourceCharacteristics characteristics,
    @Nullable ConfidenceLevel confidence,
    @NonNull ProductionSourceEvidenceReference evidenceReference
) {

  public AvailableProductionSource {
    requireNonNull(reference, "production source reference cannot be null");
    requireNonNull(availability, "production source material availability cannot be null");
    requireNonNull(characteristics, "available production source characteristics cannot be null");
    requireNonNull(evidenceReference, "production source evidence reference cannot be null");
  }

  public @NonNull VolumeValue selectedVolume() {
    return availability.requestedVolume();
  }

  public @NonNull VolumeValue availableVolume() {
    return availability.availableVolume();
  }

  public @NonNull MaterialStateId materialStateId() {
    return availability.materialStateId();
  }
}
