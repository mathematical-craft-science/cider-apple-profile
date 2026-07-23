package org.craft.mcs.cap.core.production.domain.model.value;


/**
 * State of the primary alcoholic fermentation cycle for a production batch.
 *
 * <p>Secondary fermentation and malolactic fermentation are separate future process cycles and
 * must not be encoded by this state.
 */
public enum AlcoholicFermentationState {

  /** Primary alcoholic fermentation has not been confirmed as started. */
  NOT_STARTED,

  /** Primary alcoholic fermentation is currently active. */
  ACTIVE,

  /** Fermentation stopped before the technologist confirmed completion and may be resumed. */
  ARRESTED,

  /** The technologist confirmed completion of the primary alcoholic fermentation cycle. */
  COMPLETED
}
