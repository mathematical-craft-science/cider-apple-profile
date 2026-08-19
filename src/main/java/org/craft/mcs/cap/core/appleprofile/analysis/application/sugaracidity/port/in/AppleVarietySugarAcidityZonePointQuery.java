package org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.port.in;

import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.model.AppleVarietyAnalysisFilters;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.model.AppleVarietyAnalysisPagePosition;
import org.craft.mcs.cap.core.profileanalysis.graph.application.model.JolicoeurSugarAcidityGraphCriteria;
import org.craft.mcs.cap.shared.application.query.pagination.CursorPageRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record AppleVarietySugarAcidityZonePointQuery(
    @NonNull AppleVarietyAnalysisFilters filters,
    @Nullable JolicoeurSugarAcidityGraphCriteria graphCriteria,
    @NonNull CursorPageRequest<@NonNull AppleVarietyAnalysisPagePosition> page
) {

  public AppleVarietySugarAcidityZonePointQuery {
    requireNonNull(filters, "apple variety analysis filters cannot be null");
    requireNonNull(page, "cursor page request cannot be null");
  }

  public static @NonNull AppleVarietySugarAcidityZonePointQuery firstPage() {
    return new AppleVarietySugarAcidityZonePointQuery(
        new AppleVarietyAnalysisFilters(null),
        null,
        CursorPageRequest.firstPage()
    );
  }
}
