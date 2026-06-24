package org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in;


/**
 * Semantic severity of one planning overview signal.
 *
 * <p>This value describes how important the signal is for feedstock planning. API clients decide
 * how to render it visually.
 */
public enum FeedstockPlanningSignalSeverity {

  /**
   * The checked planning condition is satisfied.
   */
  OK,

  /**
   * The signal is informational and does not require action by itself.
   */
  INFO,

  /**
   * The signal points to incomplete, stale or lower-confidence evidence, but does not block planning.
   */
  WARNING,

  /**
   * The signal blocks the current planning scenario until the missing or invalid condition is resolved.
   */
  BLOCKING
}
