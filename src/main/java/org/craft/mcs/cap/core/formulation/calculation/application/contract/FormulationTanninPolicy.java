package org.craft.mcs.cap.core.formulation.calculation.application.contract;


/** Policy controlling whether incomplete tannin evidence may participate in a composition calculation. */
public enum FormulationTanninPolicy {
  IGNORE_TANNIN,
  CALCULATE_WHEN_COMPLETE,
  REQUIRE_TANNIN
}
