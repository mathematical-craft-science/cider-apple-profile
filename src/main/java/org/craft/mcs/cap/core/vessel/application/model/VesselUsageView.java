package org.craft.mcs.cap.core.vessel.application.model;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.vessel.application.VesselUsageProjectionException;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Vessel catalog data enriched with its derived current material usage.
 *
 * <p>Only the catalog and current-content reference are stored in this read model. Used volume,
 * remaining capacity and classification are derived so they cannot contradict the source facts.</p>
 */
public record VesselUsageView(
    @NonNull VesselView vessel,
    @Nullable VesselCurrentContentView currentContent
) {

  public VesselUsageView {
    requireNonNull(vessel, "vessel view cannot be null");
    if (currentContent != null) {
      if (!vessel.vesselId().equals(currentContent.vesselId())) {
        throw new VesselUsageProjectionException("current content belongs to another vessel");
      }
      if (currentContent.volume().exceeds(vessel.nominalCapacity())) {
        throw new VesselUsageProjectionException("current content exceeds vessel nominal capacity");
      }
    }
  }

  public static @NonNull VesselUsageView empty(@NonNull VesselView vessel) {
    return new VesselUsageView(vessel, null);
  }

  public @NonNull VolumeValue usedVolume() {
    return currentContent == null
        ? VolumeValue.ofLiters(BigDecimal.ZERO)
        : currentContent.volume();
  }

  public @NonNull VolumeValue remainingCapacity() {
    return vessel.nominalCapacity().subtract(usedVolume());
  }

  public @NonNull VesselUsageClassification usage() {
    if (currentContent == null) {
      return VesselUsageClassification.EMPTY;
    }
    return currentContent.volume().equals(vessel.nominalCapacity())
        ? VesselUsageClassification.FULL
        : VesselUsageClassification.PARTIALLY_FILLED;
  }
}
