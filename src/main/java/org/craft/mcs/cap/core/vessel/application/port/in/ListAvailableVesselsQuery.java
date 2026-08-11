package org.craft.mcs.cap.core.vessel.application.port.in;

import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.application.query.TextSearchCriteria;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Selection criteria for operational empty vessels by name and required material volume. */
public record ListAvailableVesselsQuery(
    @NonNull WorkspaceId workspaceId,
    @Nullable TextSearchCriteria nameContains,
    @Nullable VolumeValue requiredVolume
) {

  public ListAvailableVesselsQuery {
    requireNonNull(workspaceId, "workspace id cannot be null");
    if (requiredVolume != null && requiredVolume.isZero()) {
      throw new ApplicationValidationException("required vessel volume must be greater than zero");
    }
  }
}
