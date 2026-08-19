package org.craft.mcs.cap.core.production.batch.application.model;


/** Identifies the authoritative source of one current Production characteristic. */
public enum ProductionCharacteristicOrigin {

  /** Value remains inherited from the calculated profile accepted when the batch was created. */
  INITIAL_SNAPSHOT,

  /** Value is supplied by state-aware measurement evidence for the current material lineage. */
  MEASUREMENT
}
