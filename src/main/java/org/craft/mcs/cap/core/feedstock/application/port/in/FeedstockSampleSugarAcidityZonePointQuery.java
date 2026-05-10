package org.craft.mcs.cap.core.feedstock.application.port.in;

import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.shared.platform.query.CursorPageRequest;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record FeedstockSampleSugarAcidityZonePointQuery(
    @NonNull CiderMakerId ciderMakerId,
    @NonNull FeedstockSamplePointFilters filters,
    @NonNull CursorPageRequest page
) {

  public FeedstockSampleSugarAcidityZonePointQuery {
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
    requireNonNull(filters, "feedstock sample point filters cannot be null");
    requireNonNull(page, "cursor page request cannot be null");
  }
}
