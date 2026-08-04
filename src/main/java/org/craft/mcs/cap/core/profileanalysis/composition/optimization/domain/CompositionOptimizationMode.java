package org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain;


/**
 * Defines what kind of weights the optimizer should produce.
 *
 * <p>This is an explicit domain decision and must not be inferred from optional volume fields. In
 * {@link #PROPORTION} mode the optimizer returns normalized fractions in the range {@code (0, 1]}.
 * In {@link #ABSOLUTE_VOLUME} mode it converts internally enumerated percentage shares into liters
 * using the requested total volume and validates them against source availability when it is known.
 */
public enum CompositionOptimizationMode {

  /**
   * Optimize source shares as normalized proportions whose sum is one.
   */
  PROPORTION,

  /**
   * Optimize source shares for a concrete target volume. Candidate weights are returned as liters
   * and bounded by each source available volume when it is known.
   */
  ABSOLUTE_VOLUME
}
