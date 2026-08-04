package org.craft.mcs.cap.shared.application.measurement;


/** Freshness of selected measurement evidence relative to the applicable workspace policy. */
public enum MeasurementFreshnessStatus {

  /** The evidence remains within the configured freshness period. */
  CURRENT,

  /** The evidence has reached or exceeded the configured stale threshold. */
  STALE
}
