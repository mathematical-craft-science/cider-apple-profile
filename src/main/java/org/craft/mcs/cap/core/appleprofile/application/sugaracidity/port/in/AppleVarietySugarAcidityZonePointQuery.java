package org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in;

import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.model.AppleVarietySugarAcidityPagePosition;
import org.craft.mcs.cap.shared.application.query.pagination.CursorPageRequest;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record AppleVarietySugarAcidityZonePointQuery(
    @NonNull AppleVarietyPointFilters filters,
    @NonNull CursorPageRequest<@NonNull AppleVarietySugarAcidityPagePosition> page
) {

  public AppleVarietySugarAcidityZonePointQuery {
    requireNonNull(filters, "apple variety point filters cannot be null");
    requireNonNull(page, "cursor page request cannot be null");
  }

  public static @NonNull AppleVarietySugarAcidityZonePointQuery firstPage() {
    return new AppleVarietySugarAcidityZonePointQuery(
        new AppleVarietyPointFilters(null, null, null),
        CursorPageRequest.firstPage()
    );
  }
}
