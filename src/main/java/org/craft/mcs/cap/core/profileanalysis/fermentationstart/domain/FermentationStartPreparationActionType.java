package org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain;


/**
 * Preparation action that can explain a managed fermentation-start risk.
 *
 * <p>Planned actions do not change current measured characteristics. Only a confirmed operation or a
 * new measurement can change the values used by readiness assessment.
 */
public enum FermentationStartPreparationActionType {

  /**
   * A yeast strain has been selected. Without a strain profile this is only contextual evidence.
   */
  YEAST_SELECTED,

  /**
   * SO2 addition is planned as microbial-risk management for high pH.
   */
  SO2_ADDITION_PLANNED,

  /**
   * SO2 addition has been confirmed as a performed preparation action.
   */
  SO2_ADDITION_CONFIRMED,

  /**
   * Acid adjustment is planned to reduce pH or improve acidity balance.
   */
  ACID_ADJUSTMENT_PLANNED,

  /**
   * Acid adjustment has been confirmed as a performed preparation action.
   */
  ACID_ADJUSTMENT_CONFIRMED,

  /**
   * pH has been measured again after preparation.
   */
  PH_REMEASURED,

  /**
   * Yeast nutrient addition is planned for low YAN management.
   */
  YEAST_NUTRIENT_ADDITION_PLANNED,

  /**
   * Yeast nutrient addition has been confirmed as a performed preparation action.
   */
  YEAST_NUTRIENT_ADDITION_CONFIRMED,

  /**
   * Aeration or oxygen exposure is planned to support yeast health; it does not add YAN itself.
   */
  OXYGENATION_PLANNED,

  /**
   * Aeration or oxygen exposure has been confirmed as a performed preparation action.
   */
  OXYGENATION_CONFIRMED,

  /**
   * YAN has been measured again after preparation.
   */
  YAN_REMEASURED
}
