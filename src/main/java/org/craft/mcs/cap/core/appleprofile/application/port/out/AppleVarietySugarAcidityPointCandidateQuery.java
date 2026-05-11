package org.craft.mcs.cap.core.appleprofile.application.port.out;

import org.craft.mcs.cap.shared.platform.query.CursorPageRequest;
import org.craft.mcs.cap.shared.platform.query.TextSearchCriteria;
import org.jspecify.annotations.Nullable;


public record AppleVarietySugarAcidityPointCandidateQuery(
    @Nullable TextSearchCriteria nameContains,
    @Nullable AppleVarietySugarAcidityPointCandidateCursor after,
    int limit
) {

  public AppleVarietySugarAcidityPointCandidateQuery {
    if (limit < 1 || limit > CursorPageRequest.MAX_LIMIT + 1) {
      throw new IllegalArgumentException("candidate query limit must be between 1 and " + (CursorPageRequest.MAX_LIMIT + 1));
    }
  }
}
