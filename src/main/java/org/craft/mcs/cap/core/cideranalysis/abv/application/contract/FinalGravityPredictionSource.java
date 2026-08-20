package org.craft.mcs.cap.core.cideranalysis.abv.application.contract;


/** Source of the final-gravity assumption used by an ABV prediction. */
public enum FinalGravityPredictionSource {
  FIXED_DRY_CIDER_DEFAULT,
  YEAST_CHARACTERISTICS,
  FERMENTATION_MODEL,
  FERMENTATION_MEASUREMENTS
}
