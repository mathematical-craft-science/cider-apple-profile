package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto;


/** Transport policy for missing optimization criterion inputs. */
public enum CompositionOptimizationMissingPolicy {
  IGNORE_IF_MISSING,
  PENALIZE_IF_MISSING,
  EXCLUDE_IF_MISSING
}
