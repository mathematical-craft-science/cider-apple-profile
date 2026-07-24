package org.craft.mcs.cap.core.measurement.application.model;


/** Freshness of one selected measurement record relative to workspace policy. */
public enum MeasurementFreshnessStatus {

  /** The record is newer than the configured stale threshold. */
  CURRENT,

  /** The record has reached or exceeded the configured stale threshold. */
  STALE
}
