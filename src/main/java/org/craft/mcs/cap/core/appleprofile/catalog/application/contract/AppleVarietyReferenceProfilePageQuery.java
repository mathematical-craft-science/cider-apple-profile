package org.craft.mcs.cap.core.appleprofile.catalog.application.contract;

import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.application.query.QueryLimits;
import org.craft.mcs.cap.shared.application.query.TextSearchCriteria;
import org.jspecify.annotations.Nullable;


public record AppleVarietyReferenceProfilePageQuery(
    @Nullable TextSearchCriteria nameContains,
    @Nullable AppleVarietyCatalogPagePosition after,
    int limit
) {

  public AppleVarietyReferenceProfilePageQuery {
    if (limit < 1 || limit > QueryLimits.MAXIMUM_PAGE_FETCH) {
      throw new ApplicationValidationException(
          "reference profile page limit must be between 1 and " + QueryLimits.MAXIMUM_PAGE_FETCH
      );
    }
  }
}
