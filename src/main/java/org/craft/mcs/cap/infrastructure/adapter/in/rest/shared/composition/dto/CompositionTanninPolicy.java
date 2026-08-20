package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto;


/** Transport policy for incomplete tannin source data. */
public enum CompositionTanninPolicy {
  IGNORE_TANNIN,
  CALCULATE_WHEN_COMPLETE,
  REQUIRE_TANNIN
}
