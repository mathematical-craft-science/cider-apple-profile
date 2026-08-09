package org.craft.mcs.cap.core.vessel.domain.model;


/** Construction material relevant to vessel compatibility, handling and maintenance. */
public enum VesselMaterial {

  /** Stainless-steel vessel. */
  STAINLESS_STEEL,

  /** Food-grade polymer vessel. */
  FOOD_GRADE_PLASTIC,

  /** Glass vessel. */
  GLASS,

  /** Wooden vessel, including barrels and vats. */
  WOOD,

  /** Concrete vessel. */
  CONCRETE,

  /** Material outside the currently modeled catalog. */
  OTHER
}
