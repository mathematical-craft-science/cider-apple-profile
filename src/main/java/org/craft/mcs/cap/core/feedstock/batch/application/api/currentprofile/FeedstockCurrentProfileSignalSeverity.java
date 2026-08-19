package org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile;


/**
 * Semantic severity of one current-profile signal.
 *
 * <p>This value describes how important the signal is for feedstock assessment. API clients decide
 * how to render it visually.
 */
public enum FeedstockCurrentProfileSignalSeverity {

  /**
   * The checked condition is satisfied.
   */
  OK,

  /**
   * The signal is informational and does not require action by itself.
   */
  INFO,

  /**
   * The signal points to incomplete, stale or lower-confidence evidence, but does not block assessment.
   */
  WARNING,

  /**
   * The signal blocks the current scenario until the missing or invalid condition is resolved.
   */
  BLOCKING
}
