package org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Calculated chemical profile of a source composition.
 *
 * <p>The composition keeps the exact sources used for calculation, typed canonical result
 * characteristics, optional total volume when source weights are concrete volumes, and warnings
 * for intentionally partial characteristics such as incomplete tannin or YAN.
 */
public record ChemicalProfileComposition(
    @NonNull List<@NonNull ChemicalProfileCompositionSource> sources,
    @NonNull CalculatedCompositionCharacteristics characteristics,
    @Nullable VolumeValue totalVolume,
    @NonNull List<@NonNull ChemicalProfileCompositionWarning> warnings
) {

  public ChemicalProfileComposition {
    sources = List.copyOf(requireNonNull(sources, "chemical profile composition sources cannot be null"));
    requireNonNull(characteristics, "calculated composition characteristics cannot be null");
    warnings = List.copyOf(requireNonNull(warnings, "chemical profile composition warnings cannot be null"));

    if (sources.isEmpty()) {
      throw new InvalidChemicalProfileCompositionException(
          "chemical profile composition must contain at least one source"
      );
    }
    requireUniqueSources(sources);
    requireConsistentWeightMode(sources);
    requireConsistentTotalVolume(sources, totalVolume);
  }

  public static @NonNull ChemicalProfileComposition of(
      @NonNull List<@NonNull ChemicalProfileCompositionSource> sources,
      @NonNull CalculatedCompositionCharacteristics characteristics,
      @NonNull List<@NonNull ChemicalProfileCompositionWarning> warnings
  ) {
    return new ChemicalProfileComposition(
        sources,
        characteristics,
        totalVolume(sources),
        warnings
    );
  }

  public boolean hasConcreteVolumes() {
    return totalVolume != null;
  }

  private static @Nullable VolumeValue totalVolume(
      @NonNull List<@NonNull ChemicalProfileCompositionSource> sources
  ) {
    var total = BigDecimal.ZERO;

    for (final var source : sources) {
      if (!(source.weight() instanceof AbsoluteCompositionVolume(VolumeValue volume))) {
        return null;
      }
      total = total.add(volume.liters());
    }

    return VolumeValue.ofLiters(total);
  }

  private static void requireUniqueSources(
      @NonNull List<@NonNull ChemicalProfileCompositionSource> sources
  ) {
    if (sources.stream().map(ChemicalProfileCompositionSource::reference).distinct().count() != sources.size()) {
      throw new InvalidChemicalProfileCompositionException(
          "calculated composition must contain each source exactly once"
      );
    }
  }

  private static void requireConsistentWeightMode(
      @NonNull List<@NonNull ChemicalProfileCompositionSource> sources
  ) {
    final var weightType = sources.getFirst().weight().getClass();
    if (sources.stream().anyMatch(source -> source.weight().getClass() != weightType)) {
      throw new InvalidChemicalProfileCompositionException(
          "calculated composition cannot mix absolute volumes and proportions"
      );
    }
  }

  private static void requireConsistentTotalVolume(
      @NonNull List<@NonNull ChemicalProfileCompositionSource> sources,
      @Nullable VolumeValue declaredTotalVolume
  ) {
    final var calculatedTotalVolume = totalVolume(sources);
    if (calculatedTotalVolume == null && declaredTotalVolume != null) {
      throw new InvalidChemicalProfileCompositionException(
          "proportion-based composition cannot declare a concrete total volume"
      );
    }
    if (calculatedTotalVolume != null
        && (declaredTotalVolume == null || !calculatedTotalVolume.equals(declaredTotalVolume))) {
      throw new InvalidChemicalProfileCompositionException(
          "composition total volume must equal the sum of absolute source volumes"
      );
    }
  }

}
