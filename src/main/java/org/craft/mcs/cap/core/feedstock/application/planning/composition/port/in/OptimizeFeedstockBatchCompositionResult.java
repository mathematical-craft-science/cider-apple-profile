package org.craft.mcs.cap.core.feedstock.application.planning.composition.port.in;

import org.craft.mcs.cap.shared.application.ApplicationValidationException;

import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.port.in.OptimizeChemicalProfileCompositionResult;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record OptimizeFeedstockBatchCompositionResult(
    int sourceCount,
    @NonNull OptimizeChemicalProfileCompositionResult optimization
) {

  public OptimizeFeedstockBatchCompositionResult {
    if (sourceCount < 0) {
      throw new ApplicationValidationException("feedstock composition optimization source count cannot be negative");
    }
    requireNonNull(optimization, "chemical profile composition optimization result cannot be null");
  }
}
