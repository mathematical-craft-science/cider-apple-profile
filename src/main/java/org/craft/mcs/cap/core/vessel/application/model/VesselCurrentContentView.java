package org.craft.mcs.cap.core.vessel.application.model;

import java.util.UUID;
import org.craft.mcs.cap.core.vessel.application.VesselUsageProjectionException;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Read-side reference to the single active material batch currently occupying a vessel.
 *
 * <p>The reference deliberately uses a neutral subject type and identifier so the Vessel context
 * does not depend on Feedstock or Production domain models.</p>
 */
public record VesselCurrentContentView(
    @NonNull VesselId vesselId,
    @NonNull VesselCurrentContentType subjectType,
    @NonNull UUID subjectId,
    @NonNull MaterialStateId materialStateId,
    @NonNull BatchName displayName,
    @NonNull VolumeValue volume
) {

  public VesselCurrentContentView {
    requireNonNull(vesselId, "vessel id cannot be null");
    requireNonNull(subjectType, "vessel content subject type cannot be null");
    requireNonNull(subjectId, "vessel content subject id cannot be null");
    requireNonNull(materialStateId, "vessel content material state id cannot be null");
    requireNonNull(displayName, "vessel content display name cannot be null");
    requireNonNull(volume, "vessel content volume cannot be null");
    if (volume.isZero()) {
      throw new VesselUsageProjectionException("active vessel content volume must be greater than zero");
    }
  }
}
