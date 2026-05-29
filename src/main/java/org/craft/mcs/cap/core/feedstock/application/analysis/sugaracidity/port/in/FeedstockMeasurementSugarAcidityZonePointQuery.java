package org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in;

import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.shared.platform.query.CursorPageRequest;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record FeedstockMeasurementSugarAcidityZonePointQuery(
    @NonNull CiderMakerId ciderMakerId,
    @NonNull FeedstockMeasurementPointFilters filters,
    @NonNull CursorPageRequest page
) {

  public FeedstockMeasurementSugarAcidityZonePointQuery {
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
    requireNonNull(filters, "feedstock measurement point filters cannot be null");
    requireNonNull(page, "cursor page request cannot be null");
  }
}
