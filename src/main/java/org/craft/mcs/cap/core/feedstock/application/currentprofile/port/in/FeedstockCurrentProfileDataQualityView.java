package org.craft.mcs.cap.core.feedstock.application.currentprofile.port.in;

import java.util.List;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Data-quality summary for the current feedstock profile.
 *
 * <p>This view gives clients a compact set of facts for read-model presentation while preserving
 * machine-readable codes for filtering and actions.
 */
public record FeedstockCurrentProfileDataQualityView(
    @NonNull FeedstockCurrentProfileDataQualityStatus status,
    @NonNull List<@NonNull FeedstockCurrentProfileDataQualityIndicator> indicators
) {

  public FeedstockCurrentProfileDataQualityView {
    requireNonNull(status, "feedstock current profile data quality status cannot be null");
    indicators = List.copyOf(requireNonNull(
        indicators,
        "feedstock current profile data quality indicators cannot be null"
    ));
  }
}
