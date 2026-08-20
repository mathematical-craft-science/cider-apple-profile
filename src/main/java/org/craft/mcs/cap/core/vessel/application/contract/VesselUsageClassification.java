package org.craft.mcs.cap.core.vessel.application.contract;


/** Derived capacity usage classification for a vessel read model. */
public enum VesselUsageClassification {

  /** The vessel has no active material batch. */
  EMPTY,

  /** The vessel contains one active batch below nominal capacity. */
  PARTIALLY_FILLED,

  /** The active batch volume equals nominal capacity. */
  FULL
}
