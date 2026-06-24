package org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in;

import java.util.List;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Data quality summary for one feedstock planning row.
 *
 * <p>This view gives clients a compact set of facts for board/table presentation while preserving
 * machine-readable codes for filtering and actions.
 */
public record FeedstockPlanningDataQualityView(
    @NonNull FeedstockPlanningDataQualityStatus status,
    @NonNull List<@NonNull FeedstockPlanningDataQualityIndicator> indicators
) {

  public FeedstockPlanningDataQualityView {
    requireNonNull(status, "feedstock planning data quality status cannot be null");
    indicators = List.copyOf(requireNonNull(indicators, "feedstock planning data quality indicators cannot be null"));
  }
}
