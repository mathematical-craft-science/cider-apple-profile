package org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain;


/** Defines how incomplete source tannin evidence affects composition calculation. */
public enum CompositionTanninPolicy {

  /** Excludes tannin from the result even when every source provides it. */
  IGNORE_TANNIN,

  /** Requires tannin for every source and rejects the calculation when any value is missing. */
  REQUIRE_TANNIN,

  /** Calculates tannin only when every source provides it; otherwise returns an incomplete warning. */
  CALCULATE_WHEN_COMPLETE
}
