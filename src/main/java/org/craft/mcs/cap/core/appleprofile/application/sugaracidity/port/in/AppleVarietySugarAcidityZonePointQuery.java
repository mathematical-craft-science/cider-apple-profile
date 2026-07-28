package org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in;

import org.craft.mcs.cap.core.appleprofile.application.analysis.common.model.AppleVarietyAnalysisFilters;
import org.craft.mcs.cap.core.appleprofile.application.catalog.model.AppleVarietyCatalogPagePosition;
import org.craft.mcs.cap.shared.application.query.pagination.CursorPageRequest;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record AppleVarietySugarAcidityZonePointQuery(
    @NonNull AppleVarietyAnalysisFilters filters,
    @NonNull CursorPageRequest<@NonNull AppleVarietyCatalogPagePosition> page
) {

  public AppleVarietySugarAcidityZonePointQuery {
    requireNonNull(filters, "apple variety analysis filters cannot be null");
    requireNonNull(page, "cursor page request cannot be null");
  }

  public static @NonNull AppleVarietySugarAcidityZonePointQuery firstPage() {
    return new AppleVarietySugarAcidityZonePointQuery(
        new AppleVarietyAnalysisFilters(null, null, null),
        CursorPageRequest.firstPage()
    );
  }
}
