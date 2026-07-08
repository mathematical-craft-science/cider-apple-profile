package org.craft.mcs.cap.core.feedstock.application.currentprofile.port.in;


/**
 * Aggregated data-quality status for the current feedstock profile.
 */
public enum FeedstockCurrentProfileDataQualityStatus {

  /**
   * The current evidence is sufficient for profile data-quality purposes.
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
