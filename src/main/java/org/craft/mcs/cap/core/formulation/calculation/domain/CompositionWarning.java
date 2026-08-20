package org.craft.mcs.cap.core.formulation.calculation.domain;


/** Typed explanation of a characteristic intentionally absent from a calculated composition. */
public enum CompositionWarning {

  /** Source tannin evidence existed but the selected policy explicitly excluded tannin. */
  TANNIN_IGNORED,

  /** At least one, but not all, sources provided tannin evidence. */
  TANNIN_INCOMPLETE,

  /** At least one, but not all, sources provided YAN evidence. */
  YEAST_ASSIMILABLE_NITROGEN_INCOMPLETE
}
