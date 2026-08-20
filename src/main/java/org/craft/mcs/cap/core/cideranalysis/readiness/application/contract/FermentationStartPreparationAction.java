package org.craft.mcs.cap.core.cideranalysis.readiness.application.contract;


/** Preparation action that can manage a fermentation-start readiness risk without changing measured evidence. */
public enum FermentationStartPreparationAction {
  YEAST_SELECTED,
  SO2_ADDITION_PLANNED,
  SO2_ADDITION_CONFIRMED,
  ACID_ADJUSTMENT_PLANNED,
  ACID_ADJUSTMENT_CONFIRMED,
  PH_REMEASURED,
  YEAST_NUTRIENT_ADDITION_PLANNED,
  YEAST_NUTRIENT_ADDITION_CONFIRMED,
  OXYGENATION_PLANNED,
  OXYGENATION_CONFIRMED,
  YAN_REMEASURED
}
