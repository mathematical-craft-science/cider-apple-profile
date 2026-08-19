package org.craft.mcs.cap.core.appleprofile.analysis.application.profile.model;

import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.application.query.QueryLimits;
import org.craft.mcs.cap.shared.application.query.TextSearchCriteria;
import org.jspecify.annotations.Nullable;


/** Analysis-owned source selection for one ordered catalog-profile page. */
public record AppleVarietyAnalysisPageQuery(
    @Nullable TextSearchCriteria nameContains,
    @Nullable AppleVarietyAnalysisPagePosition after,
    int limit
) {

  public AppleVarietyAnalysisPageQuery {
    if (limit < 1 || limit > QueryLimits.MAXIMUM_PAGE_FETCH) {
      throw new ApplicationValidationException(
          "analysis profile page limit must be between 1 and " + QueryLimits.MAXIMUM_PAGE_FETCH
      );
    }
  }
}
