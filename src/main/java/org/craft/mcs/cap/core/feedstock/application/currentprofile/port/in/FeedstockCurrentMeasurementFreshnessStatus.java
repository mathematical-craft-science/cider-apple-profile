package org.craft.mcs.cap.core.feedstock.application.currentprofile.port.in;


/**
 * Freshness state of the latest measurement used as the current feedstock profile evidence.
 */
public enum FeedstockCurrentMeasurementFreshnessStatus {

  /**
   * The measurement is within the configured freshness threshold.
   */
  CURRENT,

  /**
   * The measurement is older than the configured freshness threshold and may no longer represent the batch state.
   */
  STALE
}
