package org.craft.mcs.cap.core.appleprofile.application.catalog.port.out;

import org.craft.mcs.cap.core.appleprofile.application.catalog.model.AppleVarietyCatalogPagePosition;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.application.query.QueryLimits;
import org.craft.mcs.cap.shared.application.query.TextSearchCriteria;
import org.jspecify.annotations.Nullable;


public record AppleVarietyAnalysisCandidateCriteria(
    @Nullable TextSearchCriteria nameContains,
    @Nullable AppleVarietyCatalogPagePosition after,
    int limit
) {

  public AppleVarietyAnalysisCandidateCriteria {
    if (limit < 1 || limit > QueryLimits.MAXIMUM_PAGE_FETCH) {
      throw new ApplicationValidationException(
          "candidate criteria limit must be between 1 and " + QueryLimits.MAXIMUM_PAGE_FETCH
      );
    }
  }
}
