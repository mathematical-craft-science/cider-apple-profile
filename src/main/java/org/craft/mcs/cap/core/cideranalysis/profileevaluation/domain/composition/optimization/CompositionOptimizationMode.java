package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.optimization;


/**
 * Defines what kind of weights the optimizer should produce.
 *
 * <p>This is an explicit domain decision and must not be inferred from optional volume fields. In
 * {@link #PROPORTION} mode the optimizer returns relative percentages. In {@link #ABSOLUTE_VOLUME}
 * mode it converts those percentages into liters using the requested total volume and validates them
 * against source availability when it is known.
 */
public enum CompositionOptimizationMode {

  /**
   * Optimize source shares as relative proportions. Candidate weights are returned as percentages.
   */
  PROPORTION,

  /**
   * Optimize source shares for a concrete target volume. Candidate weights are returned as liters
   * and bounded by each source available volume when it is known.
   */
  ABSOLUTE_VOLUME
}
