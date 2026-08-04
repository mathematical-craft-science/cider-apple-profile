package org.craft.mcs.cap.shared.kernel.chemical;


/**
 * Expresses confidence in the evidence supporting a chemical characteristic value.
 *
 * <p>The level describes the reliability of the value's source and provenance. It does not express whether the
 * characteristic is technologically desirable and does not replace measurement uncertainty.</p>
 */
public enum ConfidenceLevel {

  /**
   * The value is based on weak, incomplete, or insufficiently verifiable evidence.
   */
  LOW,

  /**
   * The value has credible supporting evidence, but its provenance or measurement conditions are only partly known.
   */
  MEDIUM,

  /**
   * The value is supported by reliable provenance and sufficiently controlled measurement conditions.
   */
  HIGH,

  /**
   * The value is supported by a documented laboratory result from an identified source.
   */
  LAB_VERIFIED
}
