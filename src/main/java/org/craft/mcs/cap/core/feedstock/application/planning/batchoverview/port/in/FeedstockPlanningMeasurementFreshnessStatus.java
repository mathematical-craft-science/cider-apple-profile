package org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in;


/**
 * Freshness state of the current measurement used by the planning read model.
 */
public enum FeedstockPlanningMeasurementFreshnessStatus {

  /**
   * The measurement is within the configured freshness threshold.
   */
  CURRENT,

  /**
   * The measurement is older than the configured freshness threshold and may no longer represent the batch state.
   */
  STALE
}
