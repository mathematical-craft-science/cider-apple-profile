package org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain;

import java.util.List;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Result of calculating typed characteristics for a weighted source composition.
 *
 * <p>The composition keeps the exact sources used for calculation, typed canonical result
 * characteristics, optional total volume when source weights are concrete volumes, and warnings
 * for intentionally partial characteristics such as incomplete tannin or YAN.
 */
public record CalculatedComposition(
    @NonNull List<@NonNull CompositionSource> sources,
    @NonNull CalculatedCompositionCharacteristics characteristics,
    @Nullable VolumeValue totalVolume,
    @NonNull List<@NonNull CompositionWarning> warnings
) {

  public CalculatedComposition {
    sources = List.copyOf(requireNonNull(sources, "composition sources cannot be null"));
    requireNonNull(characteristics, "calculated composition characteristics cannot be null");
    warnings = List.copyOf(requireNonNull(warnings, "composition warnings cannot be null"));

    CompositionWeightPolicy.requireValid(sources);
    requireConsistentTotalVolume(sources, totalVolume);
  }

  public static @NonNull CalculatedComposition of(
      @NonNull List<@NonNull CompositionSource> sources,
      @NonNull CalculatedCompositionCharacteristics characteristics,
      @NonNull List<@NonNull CompositionWarning> warnings
  ) {
    return new CalculatedComposition(
        sources,
        characteristics,
        CompositionWeightPolicy.totalVolume(sources),
        warnings
    );
  }

  public boolean hasConcreteVolumes() {
    return totalVolume != null;
  }

  private static void requireConsistentTotalVolume(
      @NonNull List<@NonNull CompositionSource> sources,
      @Nullable VolumeValue declaredTotalVolume
  ) {
    final var calculatedTotalVolume = CompositionWeightPolicy.totalVolume(sources);
    if (calculatedTotalVolume == null && declaredTotalVolume != null) {
      throw new InvalidCompositionException(
          "proportion-based composition cannot declare a concrete total volume"
      );
    }
    if (calculatedTotalVolume != null
        && (declaredTotalVolume == null || !calculatedTotalVolume.equals(declaredTotalVolume))) {
      throw new InvalidCompositionException(
          "composition total volume must equal the sum of absolute source volumes"
      );
    }
  }

}
