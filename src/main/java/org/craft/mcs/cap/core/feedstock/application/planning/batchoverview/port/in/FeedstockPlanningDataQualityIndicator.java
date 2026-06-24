package org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in;

import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Machine-readable data quality fact for a feedstock planning row.
 *
 * <p>The indicator is a read-side interpretation of the current batch evidence. It is intentionally
 * kept outside the feedstock aggregate because it depends on planning capabilities and current
 * measurement context.
 */
public record FeedstockPlanningDataQualityIndicator(
    @NonNull FeedstockPlanningDataQualityIndicatorCode code,
    @NonNull FeedstockPlanningSignalSeverity severity,
    @NonNull String message
) {

  public FeedstockPlanningDataQualityIndicator {
    requireNonNull(code, "feedstock planning data quality indicator code cannot be null");
    requireNonNull(severity, "feedstock planning signal severity cannot be null");
    requireNonNull(message, "feedstock planning data quality message cannot be null");
  }
}
