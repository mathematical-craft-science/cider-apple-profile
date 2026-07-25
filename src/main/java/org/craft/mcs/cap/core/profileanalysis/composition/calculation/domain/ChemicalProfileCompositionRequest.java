package org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain;

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

    if (sources.isEmpty()) {
      throw new InvalidChemicalProfileCompositionException("chemical profile composition requires at least one source");
    }
    if (sources.stream().map(ChemicalProfileCompositionSource::reference).distinct().count() != sources.size()) {
      throw new InvalidChemicalProfileCompositionException(
          "chemical profile composition must contain each source exactly once"
      );
    }

    final var weightType = sources.getFirst().weight().getClass();
    if (sources.stream().map(ChemicalProfileCompositionSource::weight).map(Object::getClass)
        .anyMatch(candidateType -> !candidateType.equals(weightType))) {
      throw new InvalidChemicalProfileCompositionException(
          "chemical profile composition cannot mix absolute volumes and proportions"
      );
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
