package org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.out;

import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.model.AppleVarietySugarAcidityPagePosition;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;

import org.craft.mcs.cap.shared.application.query.QueryLimits;
import org.craft.mcs.cap.shared.platform.query.TextSearchCriteria;
import org.jspecify.annotations.Nullable;


public record AppleVarietySugarAcidityPointCandidateQuery(
    @Nullable TextSearchCriteria nameContains,
    @Nullable AppleVarietySugarAcidityPagePosition after,
    int limit
) {

  public AppleVarietySugarAcidityPointCandidateQuery {
    if (limit < 1 || limit > QueryLimits.MAXIMUM_PAGE_FETCH) {
      throw new ApplicationValidationException(
          "candidate query limit must be between 1 and " + QueryLimits.MAXIMUM_PAGE_FETCH
      );
    }
  }
}
