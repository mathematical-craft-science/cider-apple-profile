package org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.value;

/**
 * Domain-level material form of a feedstock batch.
 *
 * <p>The enum intentionally does not encode fruit family or UI labels. Fruit origin belongs to
 * source/provenance data, while human-readable labels are a transport/UI concern.
 */
public enum FeedstockType {

  JUICE,
  MUST,
  BLEND,
  UNKNOWN
}
