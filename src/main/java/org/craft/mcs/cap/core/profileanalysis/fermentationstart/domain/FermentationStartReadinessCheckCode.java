package org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain;

/** Stable machine-readable outcomes of fermentation-start readiness checks. */
public enum FermentationStartReadinessCheckCode {

  /** Specific Gravity is missing, so fermentation potential cannot be assessed. */
  SPECIFIC_GRAVITY_MISSING,

  /** Specific Gravity is available for fermentation planning. */
  SPECIFIC_GRAVITY_PRESENT,

  /** pH is missing, so yeast-stress and microbial risks cannot be assessed. */
  PH_MISSING,

  /** pH is below the generic yeast-tolerance guidance. */
  PH_BELOW_GENERIC_YEAST_TOLERANCE,

  /** pH is below the curated cider guidance range. */
  PH_LOW,

  /** pH indicates very high microbial risk without a recorded management action. */
  PH_VERY_HIGH_MICROBIAL_RISK,

  /** Very high pH microbial risk is covered by a planned or confirmed management action. */
  PH_VERY_HIGH_MICROBIAL_RISK_MANAGED,

  /** pH indicates elevated microbial risk without a recorded management action. */
  PH_HIGH_MICROBIAL_RISK,

  /** Elevated pH microbial risk is covered by a planned or confirmed management action. */
  PH_HIGH_MICROBIAL_RISK_MANAGED,

  /** pH is within the curated cider fermentation-start guidance range. */
  PH_ACCEPTABLE,

  /** YAN is missing, so nutrient-related fermentation risk cannot be assessed. */
  YAN_MISSING,

  /** YAN is severely low without a recorded nutrient-management action. */
  YAN_SEVERELY_LOW,

  /** Severely low YAN is covered by a planned or confirmed nutrient-management action. */
  YAN_SEVERELY_LOW_MANAGED,

  /** YAN is low without a recorded nutrient-management action. */
  YAN_LOW,

  /** Low YAN is covered by a planned or confirmed nutrient-management action. */
  YAN_LOW_MANAGED,

  /** YAN is high and unnecessary nutrient additions should be avoided. */
  YAN_HIGH,

  /** YAN is within the curated cider fermentation-start guidance range. */
  YAN_ACCEPTABLE
}
