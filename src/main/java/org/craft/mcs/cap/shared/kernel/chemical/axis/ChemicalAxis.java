package org.craft.mcs.cap.shared.kernel.chemical.axis;

/**
 * Canonical numerical dimensions retained by transitional chemical-profile snapshots.
 *
 * <p>The enum names measurement evidence, not its cider-making interpretation. Sugar and acidity
 * interpretations belong to analysis projections built from these axes.</p>
 */
public enum ChemicalAxis {

  /** Specific Gravity (SG). */
  SPECIFIC_GRAVITY,

  /** Titratable Acidity (TA) in the canonical acidity basis. */
  TITRATABLE_ACIDITY,

  /** Tannin concentration. */
  TANNIN,

  /** Yeast Assimilable Nitrogen (YAN). */
  YEAST_ASSIMILABLE_NITROGEN
}
