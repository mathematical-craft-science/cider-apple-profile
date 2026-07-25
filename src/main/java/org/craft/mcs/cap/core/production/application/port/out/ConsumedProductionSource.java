package org.craft.mcs.cap.core.production.application.port.out;

import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionSourceEvidenceReference;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
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
 * aggregate or persistence model. {@code materialStateId} is the source state before consumption;
 * {@code remaining} belongs to the next state recorded by production traceability.
 */
public record ConsumedProductionSource(
    @NonNull ProductionSourceReference reference,
    @Nullable ProductionSourceDisplayName displayName,
    @NonNull VolumeValue availableBefore,
    @NonNull VolumeValue consumed,
    @NonNull VolumeValue remaining,
    @Nullable ChemicalProfileAxes profile,
    @Nullable ConfidenceLevel confidence,
    @NonNull MaterialStateId materialStateId,
    @NonNull ProductionSourceEvidenceReference evidenceReference
) {

  public ConsumedProductionSource {
    requireNonNull(reference, "production source reference cannot be null");
    requireNonNull(availableBefore, "production source volume before consumption cannot be null");
    requireNonNull(consumed, "consumed production source volume cannot be null");
    requireNonNull(remaining, "remaining production source volume cannot be null");
    requireNonNull(materialStateId, "consumed production source material state id cannot be null");
    requireNonNull(evidenceReference, "production source evidence reference cannot be null");
    if (availableBefore.liters().compareTo(consumed.liters().add(remaining.liters())) != 0) {
      throw new ApplicationValidationException(
          "production source volume before consumption must equal consumed plus remaining volume"
      );
    }
  }
}
