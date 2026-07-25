package org.craft.mcs.cap.shared.application.measurement;


/** Explains how a measured characteristic became applicable to the current material state. */
public enum MeasurementValueOrigin {

  /** The characteristic was measured in the material state identified by the evidence. */
  MEASURED,

  /** The characteristic remains applicable from an earlier state in the same material lineage. */
  INHERITED,

  /** The characteristic was produced by an authoritative calculation rather than direct measurement. */
  CALCULATED
}
