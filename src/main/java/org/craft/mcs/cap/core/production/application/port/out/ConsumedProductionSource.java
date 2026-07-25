package org.craft.mcs.cap.core.production.application.port.out;

import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionSourceEvidenceReference;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Immutable snapshot of material consumed from a production source.
 *
 * <p>The production use case records this source fact without depending on the source context's
 * aggregate or persistence model.
 */
public record ConsumedProductionSource(
    @NonNull ProductionSourceReference reference,
    @Nullable ProductionSourceDisplayName displayName,
    @NonNull VolumeValue volume,
    @Nullable ChemicalProfileAxes profile,
    @Nullable ConfidenceLevel confidence,
    @NonNull MaterialStateId materialStateId,
    @NonNull ProductionSourceEvidenceReference evidenceReference
) {

  public ConsumedProductionSource {
    requireNonNull(reference, "production source reference cannot be null");
    requireNonNull(volume, "consumed production source volume cannot be null");
    requireNonNull(materialStateId, "consumed production source material state id cannot be null");
    requireNonNull(evidenceReference, "production source evidence reference cannot be null");
  }
}
