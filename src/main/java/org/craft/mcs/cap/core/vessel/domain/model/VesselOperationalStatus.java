package org.craft.mcs.cap.core.vessel.domain.model;


/** Operational availability of a physical vessel. */
public enum VesselOperationalStatus {

  /** Vessel may receive and hold material. */
  OPERATIONAL,

  /** Vessel is temporarily unavailable and therefore cannot contain active material. */
  OUT_OF_SERVICE,

  /** Vessel was permanently removed from operation and cannot contain active material. */
  RETIRED
}
