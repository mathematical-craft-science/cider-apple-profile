package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.dto;


/** Transport role describing how a source can contribute to a selected target. */
public enum CompositionContributionRole {
  BALANCED_BASE,
  SUGAR_CONTRIBUTOR,
  SUGAR_REDUCER,
  ACIDITY_CONTRIBUTOR,
  TANNIN_CONTRIBUTOR,
  ACIDITY_REDUCER,
  TANNIN_SOFTENER,
  DILUTION_SOURCE
}
