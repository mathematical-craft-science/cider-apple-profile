package org.craft.mcs.cap.core.profileanalysis.classification.domain.engine;


/**
 * Cider-apple characteristics interpreted by classification strategies.
 *
 * <p>Sugar content is evidenced by Specific Gravity and acidity by Titratable Acidity. The enum
 * names the classification meaning rather than repeating the underlying measurement type.</p>
 */
public enum ClassificationCharacteristic {

  /** Apple sugar content evidenced by Specific Gravity (SG). */
  SUGAR_CONTENT,

  /** Apple acidity evidenced by Titratable Acidity (TA). */
  ACIDITY,

  /** Tannin concentration in canonical units. */
  TANNIN
}
