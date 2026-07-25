package org.craft.mcs.cap.shared.kernel.material.value;


/**
 * Availability lifecycle shared by physical material batches.
 *
 * <p>The lifecycle describes whether material remains available and, for terminal states, why the
 * batch no longer contains material. Technological process states such as fermentation are modeled
 * separately by their owning bounded context.</p>
 */
public enum BatchLifecycle {

  /** The batch contains material and may participate in valid operations. */
  ACTIVE,

  /** All material has left the batch through confirmed downstream operations. */
  DEPLETED,

  /** The remaining material was intentionally removed from use. */
  DISCARDED
}
