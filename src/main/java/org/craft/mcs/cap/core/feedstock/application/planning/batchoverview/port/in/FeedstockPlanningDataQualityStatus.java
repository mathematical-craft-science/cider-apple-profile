package org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in;


/**
 * Aggregated data-quality status for a feedstock planning row.
 */
public enum FeedstockPlanningDataQualityStatus {

  /**
   * The current evidence is sufficient for planning data-quality purposes.
   */
  READY,

  /**
   * The current evidence has non-blocking gaps or warnings that should be visible to the technologist.
   */
  NEEDS_ATTENTION,

  /**
   * The current evidence misses mandatory data, so the row cannot be used reliably for planning.
   */
  BLOCKED
}
