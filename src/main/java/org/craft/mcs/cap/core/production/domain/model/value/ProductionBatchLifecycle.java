package org.craft.mcs.cap.core.production.domain.model.value;


/**
 * Describes whether a confirmed production batch still represents available material.
 *
 * <p>This lifecycle is independent from fermentation and other technological processes.
 */
public enum ProductionBatchLifecycle {

  /** The batch contains material and can participate in production operations. */
  ACTIVE,

  /** All material has left the batch through valid downstream operations. */
  DEPLETED,

  /** The remaining material was intentionally removed from production. */
  DISCARDED
}
