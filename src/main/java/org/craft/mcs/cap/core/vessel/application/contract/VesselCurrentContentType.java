package org.craft.mcs.cap.core.vessel.application.contract;


/** Material batch type currently occupying a vessel. */
public enum VesselCurrentContentType {

  /** Source material managed by the Feedstock bounded context. */
  FEEDSTOCK_BATCH,

  /** Material managed by the Production bounded context. */
  PRODUCTION_BATCH
}
