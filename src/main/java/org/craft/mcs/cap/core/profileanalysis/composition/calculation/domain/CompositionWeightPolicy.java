package org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/** Enforces one canonical interpretation of source weights across composition requests and results. */
final class CompositionWeightPolicy {

  private CompositionWeightPolicy() {
  }

  static void requireValid(@NonNull List<@NonNull CompositionSource> sources) {
    if (sources.isEmpty()) {
      throw new InvalidCompositionException("composition must contain at least one source");
    }
    if (sources.stream().map(CompositionSource::reference).distinct().count() != sources.size()) {
      throw new InvalidCompositionException("composition must contain each source exactly once");
    }

    final var weightType = sources.getFirst().weight().getClass();
    if (sources.stream().anyMatch(source -> source.weight().getClass() != weightType)) {
      throw new InvalidCompositionException("composition cannot mix absolute volumes and proportions");
    }
    if (weightType == CompositionProportion.class) {
      requireNormalizedProportions(sources);
    }
  }

  static @Nullable VolumeValue totalVolume(
      @NonNull List<@NonNull CompositionSource> sources
  ) {
    if (sources.isEmpty()) {
      return null;
    }

    var liters = BigDecimal.ZERO;
    for (final var source : sources) {
      switch (source.weight()) {
        case AbsoluteCompositionVolume(VolumeValue volume) ->
            liters = liters.add(volume.liters());
        case CompositionProportion _ -> {
          return null;
        }
      }
    }
    return VolumeValue.ofLiters(liters);
  }

  private static void requireNormalizedProportions(
      @NonNull List<@NonNull CompositionSource> sources
  ) {
    final var total = sources.stream()
        .map(CompositionSource::weight)
        .map(CompositionProportion.class::cast)
        .map(CompositionProportion::value)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    if (total.compareTo(BigDecimal.ONE) != 0) {
      throw new InvalidCompositionException("composition proportions must sum to one");
    }
  }
}
