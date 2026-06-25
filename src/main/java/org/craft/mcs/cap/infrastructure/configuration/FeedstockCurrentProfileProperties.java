package org.craft.mcs.cap.infrastructure.configuration;

import java.time.Duration;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static java.util.Objects.requireNonNull;


/**
 * External configuration for feedstock current-profile and planning read models.
 *
 * <p>These settings tune read-side diagnostics without changing feedstock domain invariants.
 */
@ConfigurationProperties(prefix = "cap.feedstock.current-profile")
public record FeedstockCurrentProfileProperties(
    @NonNull MeasurementFreshnessProperties measurementFreshness
) {

  public FeedstockCurrentProfileProperties {
    requireNonNull(measurementFreshness, "feedstock measurement freshness properties cannot be null");
  }

  /**
   * Configures freshness assessment for the latest measurement used as current batch evidence.
   */
  public record MeasurementFreshnessProperties(
      @NonNull Duration staleAfter
  ) {

    public MeasurementFreshnessProperties {
      requireNonNull(staleAfter, "feedstock measurement stale duration cannot be null");
      if (staleAfter.isZero() || staleAfter.isNegative()) {
        throw new IllegalArgumentException("feedstock measurement stale duration must be positive");
      }
    }
  }
}
