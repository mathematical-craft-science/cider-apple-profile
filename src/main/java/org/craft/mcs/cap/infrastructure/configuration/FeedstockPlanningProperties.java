package org.craft.mcs.cap.infrastructure.configuration;

import java.time.Duration;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import static java.util.Objects.requireNonNull;


/**
 * External configuration for feedstock planning read models.
 *
 * <p>These settings tune planning diagnostics without changing feedstock domain invariants.
 */
@ConfigurationProperties(prefix = "cap.feedstock.planning")
public record FeedstockPlanningProperties(
    @DefaultValue @NonNull MeasurementFreshnessProperties measurementFreshness
) {

  public FeedstockPlanningProperties {
    requireNonNull(measurementFreshness, "feedstock planning measurement freshness properties cannot be null");
  }

  /**
   * Configures freshness assessment for the latest measurement used as current batch evidence.
   */
  public record MeasurementFreshnessProperties(
      @DefaultValue("14d") @NonNull Duration staleAfter
  ) {

    public MeasurementFreshnessProperties {
      requireNonNull(staleAfter, "feedstock planning measurement stale duration cannot be null");
      if (staleAfter.isZero() || staleAfter.isNegative()) {
        throw new IllegalArgumentException("feedstock planning measurement stale duration must be positive");
      }
    }
  }
}
