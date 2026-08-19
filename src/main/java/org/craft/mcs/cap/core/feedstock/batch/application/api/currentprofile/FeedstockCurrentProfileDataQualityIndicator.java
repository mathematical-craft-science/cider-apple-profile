package org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile;

import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Machine-readable data-quality fact for the current feedstock profile.
 *
 * <p>The indicator is a read-side interpretation of the current batch evidence. It is intentionally
 * kept outside the feedstock aggregate because it depends on analysis capabilities and current
 * measurement context rather than aggregate invariants.
 */
public record FeedstockCurrentProfileDataQualityIndicator(
    @NonNull FeedstockCurrentProfileDataQualityIndicatorCode code,
    @NonNull FeedstockCurrentProfileSignalSeverity severity,
    @NonNull String message
) {

  public FeedstockCurrentProfileDataQualityIndicator {
    requireNonNull(code, "feedstock current profile data quality indicator code cannot be null");
    requireNonNull(severity, "feedstock current profile signal severity cannot be null");
    requireNonNull(message, "feedstock current profile data quality message cannot be null");
  }
}
