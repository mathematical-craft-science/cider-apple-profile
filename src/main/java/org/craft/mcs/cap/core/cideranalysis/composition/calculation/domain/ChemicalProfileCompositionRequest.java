package org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain;

import java.util.List;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record ChemicalProfileCompositionRequest(
    @NonNull List<@NonNull ChemicalProfileCompositionSource> sources,
    @NonNull ChemicalProfileCompositionTanninPolicy tanninPolicy
) {

  public ChemicalProfileCompositionRequest {
    sources = List.copyOf(requireNonNull(sources, "chemical profile composition sources cannot be null"));
    requireNonNull(tanninPolicy, "chemical profile composition tannin policy cannot be null");

    if (sources.size() < 2) {
      throw new InvalidChemicalProfileCompositionException("chemical profile composition requires at least two sources");
    }
  }

  public static @NonNull ChemicalProfileCompositionRequest calculateTanninWhenComplete(
      @NonNull List<@NonNull ChemicalProfileCompositionSource> sources
  ) {
    return new ChemicalProfileCompositionRequest(
        sources,
        ChemicalProfileCompositionTanninPolicy.CALCULATE_WHEN_COMPLETE
    );
  }
}
